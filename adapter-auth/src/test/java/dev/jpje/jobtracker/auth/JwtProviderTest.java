package dev.jpje.jobtracker.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import dev.jpje.jobtracker.domain.vo.UserId;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

  private static final String SECRET = "super-secret-signing-key-for-tests";
  private static final long EXPIRATION_MS = 86_400_000L;

  private final UserId userId = UserId.generate();
  private final JwtProvider provider = new JwtProvider(
    SECRET,
    EXPIRATION_MS,
    Clock.fixed(Instant.now().plusSeconds(60L), ZoneOffset.UTC));

  @Test
  void shouldGenerateToken() {
    assertThat(provider.generateToken(userId)).isNotBlank();
  }

  @Test
  void shouldReturnUserIdForValidToken() {
    final var token = generateToken();

    assertThat(provider.validateToken(token)).isEqualTo(userId);
  }

  @Test
  void shouldRejectInvalidToken() {
    assertThatThrownBy(() -> provider.validateToken("not-a-jwt"))
      .isInstanceOf(JwtException.class);
  }

  @Test
  void shouldExposeKeyDerivationFailure() {
    final var cause = new IllegalStateException("boom");

    assertThat(new KeyDerivationException("SHA-512 not available", cause))
      .hasMessage("SHA-512 not available")
      .hasCause(cause);
  }

  private String generateToken() {
    return provider.generateToken(userId);
  }
}
