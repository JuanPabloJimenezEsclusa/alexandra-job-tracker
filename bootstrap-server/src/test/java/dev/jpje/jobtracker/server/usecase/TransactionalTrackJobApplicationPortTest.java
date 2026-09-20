package dev.jpje.jobtracker.server.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.inbound.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.micrometer.core.instrument.Counter;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class TransactionalTrackJobApplicationPortTest {

  private static final UserId USER_ID = UserId.generate();
  private static final UUID APPLICATION_ID = UUID.randomUUID();
  private static final UUID JOB_POSTING_ID = UUID.randomUUID();

  @Mock
  private TrackJobApplicationPort delegate;

  @Mock
  private PlatformTransactionManager transactionManager;

  @Mock
  private Counter applicationCreatedCounter;

  private TransactionalTrackJobApplicationPort applicationPort;

  @BeforeEach
  void setUp() {
    applicationPort = new TransactionalTrackJobApplicationPort(delegate,
      new TransactionTemplate(transactionManager), applicationCreatedCounter);
  }

  private static Stream<Arguments> uncountedWriteOperations() {
    return Stream.of(
      arguments(
        named("updateStatus", "updateStatus"),
        (Consumer<TransactionalTrackJobApplicationPort>) port ->
          port.updateStatus(USER_ID, APPLICATION_ID, ApplicationStatus.APPLIED, null),
        (Consumer<TrackJobApplicationPort>) given ->
          verify(given, description("updateStatus delegated to the use case"))
            .updateStatus(USER_ID, APPLICATION_ID, ApplicationStatus.APPLIED, null)),
      arguments(
        named("delete", "delete"),
        (Consumer<TransactionalTrackJobApplicationPort>) port -> port.delete(USER_ID, APPLICATION_ID),
        (Consumer<TrackJobApplicationPort>) given ->
          verify(given, description("delete delegated to the use case")).delete(USER_ID, APPLICATION_ID))
    );
  }

  @ParameterizedTest(name = "{0} runs in a single transaction")
  @MethodSource("uncountedWriteOperations")
  void shouldRunUncountedWriteOperationInOneTransaction(final String operationName,
                                                        final Consumer<TransactionalTrackJobApplicationPort> operation,
                                                        final Consumer<TrackJobApplicationPort> delegateVerification) {
    // Given
    when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

    // When
    operation.accept(applicationPort);

    // Then
    verify(transactionManager, description("write opens a transaction")).getTransaction(any());
    verify(transactionManager, description("write commits the transaction")).commit(any());
    delegateVerification.accept(delegate);
    verifyNoInteractions(applicationCreatedCounter);
    verifyNoMoreInteractions(delegate, transactionManager);
  }

  @Test
  void shouldRunCreateInOneTransactionAndIncrementCounterOnce() {
    // Given
    final var created = application();
    when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    when(delegate.create(USER_ID, JOB_POSTING_ID, null)).thenReturn(created);

    // When
    final var result = applicationPort.create(USER_ID, JOB_POSTING_ID, null);

    // Then
    assertThat(result).isSameAs(created);
    verify(transactionManager, description("create opens a transaction")).getTransaction(any());
    verify(transactionManager, description("create commits the transaction")).commit(any());
    verify(delegate, description("create delegated to the use case")).create(USER_ID, JOB_POSTING_ID, null);
    verify(applicationCreatedCounter, description("create increments the application counter once"))
      .increment();
    verifyNoMoreInteractions(delegate, transactionManager, applicationCreatedCounter);
  }

  @Test
  void shouldDelegateListWithoutTransaction() {
    // Given
    final var applications = List.of(application());
    when(delegate.list(USER_ID, null)).thenReturn(applications);

    // When
    final var result = applicationPort.list(USER_ID, null);

    // Then
    assertThat(result).isSameAs(applications);
    verify(delegate, description("list delegated to the use case")).list(USER_ID, null);
    verifyNoMoreInteractions(delegate);
    verifyNoInteractions(transactionManager, applicationCreatedCounter);
  }

  private static JobApplication application() {
    return Instancio.of(JobApplication.class)
      .set(field(JobApplication::userId), USER_ID)
      .set(field(JobApplication::jobPostingId), JOB_POSTING_ID)
      .set(field(JobApplication::status), ApplicationStatus.SAVED)
      .set(field(JobApplication::version), null)
      .create();
  }
}
