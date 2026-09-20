package dev.jpje.jobtracker.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.application.port.outbound.EventPublisher;
import dev.jpje.jobtracker.domain.event.DomainEvent;
import dev.jpje.jobtracker.domain.event.UserRegistered;
import dev.jpje.jobtracker.domain.port.inbound.AuthenticationPort;
import dev.jpje.jobtracker.domain.port.outbound.LoadUserPort;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
  "jwt.secret=super-secret-signing-key-for-tests",
  "spring.application.admin.enabled=false"
})
@Import({IntegrationTestConfig.class, TransactionBoundaryIntegrationTest.TransactionRecordingConfig.class})
class TransactionBoundaryIntegrationTest {

  private static final String PASSWORD = "pass";
  private static final String PUBLICATION_FAILURE = "Simulated publication failure";

  @Autowired
  private AuthenticationPort authenticationUseCase;

  @Autowired
  private LoadUserPort loadUserPort;

  @Autowired
  private RecordingEventPublisher eventPublisher;

  @Autowired
  private UserRegisteredRecorder recorder;

  @BeforeEach
  void reset() {
    eventPublisher.reset();
    recorder.reset();
  }

  @Test
  void shouldRunWriteInOneTransaction() {
    // Given
    final var username = Username.of("tx-one-unit-user");

    // When
    authenticationUseCase.register(username, PASSWORD, UserRole.USER);

    // Then
    assertThat(eventPublisher.transactionActiveAtPublish())
      .as("registration publishes its event inside a single transaction")
      .containsExactly(true);
    assertThat(loadUserPort.findByUsername(username.value()))
      .as("registered user persisted").isPresent();
  }

  @Test
  void shouldDispatchUserRegisteredAfterCommit() {
    // Given
    final var username = Username.of("tx-after-commit-user");

    // When
    authenticationUseCase.register(username, PASSWORD, UserRole.USER);

    // Then
    assertThat(recorder.registeredUsernames())
      .as("user registered event dispatched after the commit")
      .containsExactly(username);
    assertThat(loadUserPort.findByUsername(username.value()))
      .as("registered user persisted").isPresent();
  }

  @Test
  void shouldRollbackWriteWhenPublicationFails() {
    // Given
    final var username = Username.of("tx-rollback-user");
    eventPublisher.failOnPublish();

    // When, then
    assertThatThrownBy(() -> authenticationUseCase.register(username, PASSWORD, UserRole.USER))
      .as("publication failure aborts registration")
      .isInstanceOf(IllegalStateException.class)
      .hasMessage(PUBLICATION_FAILURE);
    assertThat(loadUserPort.findByUsername(username.value()))
      .as("failed registration rolled back").isEmpty();
  }

  @TestConfiguration
  static class TransactionRecordingConfig {

    @Bean
    @Primary
    RecordingEventPublisher recordingEventPublisher(final ApplicationEventPublisher delegate) {
      return new RecordingEventPublisher(delegate);
    }

    @Bean
    UserRegisteredRecorder userRegisteredRecorder() {
      return new UserRegisteredRecorder();
    }
  }

  static final class RecordingEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher delegate;
    private final List<Boolean> transactionActiveAtPublish =
      Collections.synchronizedList(new ArrayList<>());
    private boolean failOnPublish;

    RecordingEventPublisher(final ApplicationEventPublisher delegate) {
      this.delegate = delegate;
    }

    @Override
    public void publish(final DomainEvent event) {
      transactionActiveAtPublish.add(TransactionSynchronizationManager.isActualTransactionActive());
      if (failOnPublish) {
        throw new IllegalStateException(PUBLICATION_FAILURE);
      }
      delegate.publishEvent(event);
    }

    List<Boolean> transactionActiveAtPublish() {
      return List.copyOf(transactionActiveAtPublish);
    }

    void failOnPublish() {
      this.failOnPublish = true;
    }

    void reset() {
      transactionActiveAtPublish.clear();
      this.failOnPublish = false;
    }
  }

  static final class UserRegisteredRecorder {

    private final List<UserRegistered> events = Collections.synchronizedList(new ArrayList<>());

    @TransactionalEventListener
    public void record(final UserRegistered event) {
      events.add(event);
    }

    List<Username> registeredUsernames() {
      return events.stream().map(UserRegistered::username).toList();
    }

    void reset() {
      events.clear();
    }
  }
}
