package dev.jpje.jobtracker.server.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.inbound.AuthenticationPort;
import dev.jpje.jobtracker.domain.vo.AuthPayload;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class TransactionalAuthenticationPortTest {

  private static final String USERNAME = "alice";
  private static final String PASSWORD = "pass";
  private static final String TOKEN = "jwt-token";

  @Mock
  private AuthenticationPort delegate;

  @Mock
  private PlatformTransactionManager transactionManager;

  private TransactionalAuthenticationPort authenticationPort;

  @BeforeEach
  void setUp() {
    authenticationPort = new TransactionalAuthenticationPort(delegate,
      new TransactionTemplate(transactionManager));
  }

  @Test
  void shouldRunRegistrationInTransaction() {
    // Given
    final var username = Username.of(USERNAME);
    final var payload = payload(username);
    when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    when(delegate.register(username, PASSWORD, UserRole.USER)).thenReturn(payload);

    // When
    final var result = authenticationPort.register(username, PASSWORD, UserRole.USER);

    // Then
    assertThat(result).isSameAs(payload);
    verify(transactionManager, description("registration opens a transaction")).getTransaction(any());
    verify(transactionManager, description("registration commits the transaction")).commit(any());
    verify(delegate, description("registration delegated to the use case"))
      .register(username, PASSWORD, UserRole.USER);
    verifyNoMoreInteractions(delegate, transactionManager);
  }

  @Test
  void shouldDelegateLoginWithoutTransaction() {
    // Given
    final var username = Username.of(USERNAME);
    final var payload = payload(username);
    when(delegate.login(username, PASSWORD)).thenReturn(payload);

    // When
    final var result = authenticationPort.login(username, PASSWORD);

    // Then
    assertThat(result).isSameAs(payload);
    verify(delegate, description("login delegated to the use case")).login(username, PASSWORD);
    verifyNoMoreInteractions(delegate);
    verifyNoInteractions(transactionManager);
  }

  @Test
  void shouldDelegateCurrentUserWithoutTransaction() {
    // Given
    final var userId = new UserId(UUID.randomUUID());
    final var user = user(Username.of(USERNAME), userId);
    when(delegate.getCurrentUser(userId)).thenReturn(Optional.of(user));

    // When
    final var result = authenticationPort.getCurrentUser(userId);

    // Then
    assertThat(result).hasValue(user);
    verify(delegate, description("current user lookup delegated to the use case")).getCurrentUser(userId);
    verifyNoMoreInteractions(delegate);
    verifyNoInteractions(transactionManager);
  }

  private static AuthPayload payload(final Username username) {
    return new AuthPayload(TOKEN, user(username, UserId.generate()));
  }

  private static User user(final Username username, final UserId userId) {
    return Instancio.of(User.class)
      .set(field(User::id), userId)
      .set(field(User::username), username)
      .set(field(User::passwordHash), "hash")
      .create();
  }
}
