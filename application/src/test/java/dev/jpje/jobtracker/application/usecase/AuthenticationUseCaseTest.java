package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.EventPublisher;
import dev.jpje.jobtracker.domain.event.UserRegistered;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.out.LoadUserPort;
import dev.jpje.jobtracker.domain.port.out.PasswordEncoderPort;
import dev.jpje.jobtracker.domain.port.out.SaveUserPort;
import dev.jpje.jobtracker.domain.port.out.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.Username;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    final var username = Username.of("alice");
    when(loadUserPort.findByUsername("alice")).thenReturn(Optional.empty());
    when(passwordEncoderPort.encode("pass")).thenReturn("encoded-pass");
    when(tokenGeneratorPort.generateToken(any())).thenReturn("jwt-token");
    when(clock.instant()).thenReturn(Instant.EPOCH);

    final var payload = useCase.register(username, "pass");

    assertThat(payload.user().username().value()).isEqualTo("alice");
    verify(passwordEncoderPort).encode("pass");
    verify(saveUserPort).save(payload.user());
    verify(tokenGeneratorPort).generateToken(any());
    verify(eventPublisher).publish(any(UserRegistered.class));
    verifyNoMoreInteractions(passwordEncoderPort, tokenGeneratorPort, saveUserPort, eventPublisher);
  }

  @Test
  void shouldRejectDuplicateRegistration() {
    final var existing = userWithUsername("existing");
    final var username = Username.of("alice");
    when(loadUserPort.findByUsername("alice")).thenReturn(Optional.of(existing));

    assertThatThrownBy(() -> useCase.register(username, "pass"))
      .isInstanceOf(ResourceAlreadyExistsException.class)
      .hasMessage("Username already taken");
    verifyNoMoreInteractions(passwordEncoderPort, tokenGeneratorPort, saveUserPort, eventPublisher);
  }

  @Test
  void shouldLogin() {
    final var matchingUser = userWithUsername("alice");
    when(loadUserPort.findByUsername("alice")).thenReturn(Optional.of(matchingUser));
    when(passwordEncoderPort.matches("correct-password", matchingUser.passwordHash())).thenReturn(true);
    when(tokenGeneratorPort.generateToken(any())).thenReturn("jwt-token");

    final var payload = useCase.login(Username.of("alice"), "correct-password");

    assertThat(payload.user().username().value()).isEqualTo("alice");
    verify(passwordEncoderPort).matches("correct-password", matchingUser.passwordHash());
    verifyNoMoreInteractions(passwordEncoderPort, tokenGeneratorPort);
  }

  @Test
  void shouldRejectLoginWithWrongPassword() {
    final var matchingUser = userWithUsername("alice");
    final var username = Username.of("alice");
    when(loadUserPort.findByUsername("alice")).thenReturn(Optional.of(matchingUser));
    when(passwordEncoderPort.matches("wrong-password", matchingUser.passwordHash())).thenReturn(false);

    assertThatThrownBy(() -> useCase.login(username, "wrong-password"))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Invalid credentials");
    verifyNoMoreInteractions(tokenGeneratorPort);
  }

  @Test
  void shouldRejectLoginForUnknownUser() {
    final var username = Username.of("nonexistent");
    when(loadUserPort.findByUsername("nonexistent")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.login(username, "pass"))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("Invalid credentials");
    verifyNoMoreInteractions(tokenGeneratorPort);
  }

  @Test
  void shouldReturnCurrentUser() {
    final var userId = new UserId(UUID.randomUUID());
    final var user = Instancio.of(User.class)
      .set(field(User::id), userId)
      .set(field(User::username), Username.of("alice"))
      .set(field(User::passwordHash), "hash")
      .create();
    when(loadUserPort.findById(userId)).thenReturn(Optional.of(user));

    final var result = useCase.getCurrentUser(userId);

    assertThat(result).hasValue(user);
  }
}
