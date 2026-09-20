package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.application.port.outbound.AuthenticateUserPort;
import dev.jpje.jobtracker.application.port.outbound.EventPublisher;
import dev.jpje.jobtracker.application.port.outbound.PasswordEncoderPort;
import dev.jpje.jobtracker.application.port.outbound.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.event.UserRegistered;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.outbound.LoadUserPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveUserPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthenticationUseCaseTest {

  @InjectMocks
  private AuthenticationUseCase useCase;

  @Mock
  private TokenGeneratorPort tokenGeneratorPort;

  @Mock
  private LoadUserPort loadUserPort;

  @Mock
  private SaveUserPort saveUserPort;

  @Mock
  private PasswordEncoderPort passwordEncoderPort;

  @Mock
  private AuthenticateUserPort authenticateUserPort;

  @Mock
  private Clock clock;

  @Mock
  private EventPublisher eventPublisher;

  private static User userWithUsername(final String username) {
    return Instancio.of(User.class)
      .set(field(User::username), Username.of(username))
      .set(field(User::passwordHash), "hash")
      .create();
  }

  @Test
  void shouldRegister() {
    // Given
    final var username = Username.of("alice");
    when(loadUserPort.findByUsername("alice")).thenReturn(Optional.empty());
    when(passwordEncoderPort.encode("pass")).thenReturn("encoded-pass");
    when(tokenGeneratorPort.generateToken(any(UserId.class), any(UserRole.class))).thenReturn("jwt-token");
    when(clock.instant()).thenReturn(Instant.EPOCH);

    // When
    final var payload = useCase.register(username, "pass", UserRole.USER);

    // Then
    assertThat(payload.user().username().value()).as("registered user username").isEqualTo("alice");
    assertThat(payload.user().role()).as("registered user role").isEqualTo(UserRole.USER);
    verify(passwordEncoderPort).encode("pass");
    verify(saveUserPort).save(payload.user());
    verify(tokenGeneratorPort).generateToken(any(UserId.class), any(UserRole.class));
    verify(eventPublisher).publish(any(UserRegistered.class));
    verifyNoMoreInteractions(passwordEncoderPort, tokenGeneratorPort, saveUserPort, eventPublisher);
  }

  @Test
  void shouldRejectDuplicateRegistration() {
    // Given
    final var existing = userWithUsername("existing");
    final var username = Username.of("alice");
    when(loadUserPort.findByUsername("alice")).thenReturn(Optional.of(existing));

    // When, then
    assertThatThrownBy(() -> useCase.register(username, "pass", UserRole.USER))
      .isInstanceOf(ResourceAlreadyExistsException.class)
      .hasMessage("Username already taken");
    verifyNoMoreInteractions(passwordEncoderPort, tokenGeneratorPort, saveUserPort, eventPublisher);
  }

  @Test
  void shouldLogin() {
    // Given
    final var matchingUser = userWithUsername("alice");
    when(authenticateUserPort.authenticate("alice", "correct-password")).thenReturn(matchingUser);
    when(tokenGeneratorPort.generateToken(any(UserId.class), any(UserRole.class))).thenReturn("jwt-token");

    // When
    final var payload = useCase.login(Username.of("alice"), "correct-password");

    // Then
    assertThat(payload.user().username().value()).isEqualTo("alice");
    assertThat(payload.token()).isEqualTo("jwt-token");
    verify(authenticateUserPort).authenticate("alice", "correct-password");
    verify(tokenGeneratorPort).generateToken(any(UserId.class), any(UserRole.class));
    verifyNoMoreInteractions(authenticateUserPort, tokenGeneratorPort);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("failedLogins")
  void shouldRejectInvalidLogin(final String username, final String password) {
    // Given
    when(authenticateUserPort.authenticate(username, password))
      .thenThrow(new IllegalArgumentException("Invalid credentials"));

    // When, then
    final var usernameValueObject = Username.of(username);
    assertThatThrownBy(() -> useCase.login(usernameValueObject, password))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Invalid credentials");
    verifyNoMoreInteractions(tokenGeneratorPort);
  }

  private static Stream<Arguments> failedLogins() {
    return Stream.of(
      arguments(named("wrong password", "alice"), "wrong-password"),
      arguments(named("unknown username", "nonexistent"), "pass")
    );
  }

  @Test
  void shouldReturnCurrentUser() {
    // Given
    final var userId = new UserId(UUID.randomUUID());
    final var user = Instancio.of(User.class)
      .set(field(User::id), userId)
      .set(field(User::username), Username.of("alice"))
      .set(field(User::passwordHash), "hash")
      .create();
    when(loadUserPort.findById(userId)).thenReturn(Optional.of(user));

    // When
    final var result = useCase.getCurrentUser(userId);

    // Then
    assertThat(result).hasValue(user);
  }
}
