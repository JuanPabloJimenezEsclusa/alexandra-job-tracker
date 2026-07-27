package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.out.LoadUserPort;
import dev.jpje.jobtracker.domain.port.out.SaveUserPort;
import dev.jpje.jobtracker.domain.port.out.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mindrot.jbcrypt.BCrypt;
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
  private Clock clock;

  private static String bcryptHash() {
    return BCrypt.hashpw("correct-password", BCrypt.gensalt());
  }

  private static Stream<Arguments> registerScenarios() {
    final var existingUser = Instancio.of(User.class)
      .set(field(User::username), "existing")
      .set(field(User::passwordHash), "hash")
      .create();
    return Stream.of(
      arguments(null, false),
      arguments(existingUser, true)
    );
  }

  private static Stream<Arguments> loginScenarios() {
    final var hash = bcryptHash();
    final var matchingUser = Instancio.of(User.class)
      .set(field(User::username), "alice")
      .set(field(User::passwordHash), hash)
      .create();
    return Stream.of(
      arguments("alice", "correct-password", matchingUser, false),
      arguments("alice", "wrong-password", matchingUser, true),
      arguments("nonexistent", "pass", null, true)
    );
  }

  @ParameterizedTest
  @MethodSource("registerScenarios")
  void shouldRegisterOrThrow(final @Nullable User existing, final boolean shouldThrow) {
    // Given
    when(loadUserPort.findByUsername("alice")).thenReturn(Optional.ofNullable(existing));

    if (shouldThrow) {
      // When, Then
      assertThatThrownBy(() -> useCase.register("alice", "pass"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Username already taken");
      return;
    }

    final var token = "jwt-token";
    when(tokenGeneratorPort.generateToken(any())).thenReturn(token);
    when(clock.instant()).thenReturn(Instant.EPOCH);

    // When
    final var payload = useCase.register("alice", "pass");

    // Then
    assertThat(payload.user().username()).isEqualTo("alice");
    verify(saveUserPort).save(payload.user());
  }

  @ParameterizedTest
  @MethodSource("loginScenarios")
  void shouldLoginOrThrow(final String username, final String password, final @Nullable User existing,
                          final boolean shouldThrow) {
    // Given
    when(loadUserPort.findByUsername(username)).thenReturn(Optional.ofNullable(existing));

    if (shouldThrow) {
      // When, Then
      assertThatThrownBy(() -> useCase.login(username, password))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid credentials");
      return;
    }

    // Given
    when(tokenGeneratorPort.generateToken(any())).thenReturn("jwt-token");

    // When
    final var payload = useCase.login(username, password);

    // Then
    assertThat(payload.user().username()).isEqualTo(username);
  }

  @Test
  void shouldReturnCurrentUser() {
    // Given
    final var userId = new UserId(UUID.randomUUID());
    final var user = Instancio.of(User.class)
      .set(field(User::id), userId)
      .set(field(User::username), "alice")
      .set(field(User::passwordHash), "hash")
      .create();
    when(loadUserPort.findById(userId)).thenReturn(Optional.of(user));

    // When
    final var result = useCase.getCurrentUser(userId);

    // Then
    assertThat(result).hasValue(user);
  }
}
