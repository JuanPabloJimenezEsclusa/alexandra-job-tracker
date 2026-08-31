package dev.jpje.jobtracker.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import javax.crypto.SecretKey;

import dev.jpje.jobtracker.domain.vo.TokenPayload;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
  void shouldGenerateWellFormedToken() {
    final var token = provider.generateToken(userId, UserRole.USER);
    final var claims = Jwts.parser().verifyWith(deriveKey()).build()
      .parseSignedClaims(token).getPayload();

    assertThat(token.split("\\.")).as("JWT should have three segments").hasSize(3);
    assertThat(claims.getSubject()).as("subject should be the user id").isEqualTo(userId.value().toString());
    assertThat(claims.get("role", String.class)).as("role claim should be embedded").isEqualTo("USER");
    assertThat(claims.getExpiration()).as("expiration claim should be set").isNotNull();
  }

  @Test
  void shouldReturnUserIdAndRoleForValidToken() {
    final var token = provider.generateToken(userId, UserRole.ADMIN);

    assertThat(provider.validateToken(token))
      .isEqualTo(new TokenPayload(userId, UserRole.ADMIN));
  }

  @Test
  void shouldRejectTokenWithInvalidRoleClaim() {
    final var token = unsignedTokenWithInvalidRole();

    assertThatThrownBy(() -> provider.validateToken(token))
      .isInstanceOf(IllegalArgumentException.class);
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

  private String unsignedTokenWithInvalidRole() {
    return Jwts.builder()
      .subject(userId.value().toString())
      .claim("role", "SUPERUSER")
      .signWith(deriveKey())
      .compact();
  }

  private static SecretKey deriveKey() {
    try {
      final var digest = MessageDigest.getInstance("SHA-512");
      final var hash = digest.digest(SECRET.getBytes(StandardCharsets.UTF_8));
      return Keys.hmacShaKeyFor(hash);
    } catch (final Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
