package dev.jpje.jobtracker.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.stream.Stream;
import javax.crypto.SecretKey;

import dev.jpje.jobtracker.domain.exception.InvalidTokenException;
import dev.jpje.jobtracker.domain.vo.TokenPayload;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JwtProviderTest {

  private static final String SECRET = "super-secret-signing-key-for-tests";
  private static final long EXPIRATION_MS = 1_800_000L;
  private static final Instant FIXED_NOW = Instant.parse("2099-01-01T00:00:00Z");
  private static final Instant PAST_INSTANT = Instant.parse("2000-01-01T00:00:00Z");

  private final UserId userId = UserId.generate();
  private final JwtProvider provider = new JwtProvider(
    SECRET,
    EXPIRATION_MS,
    Clock.fixed(FIXED_NOW, ZoneOffset.UTC));

  @Test
  void shouldGenerateWellFormedToken() {
    final var token = provider.generateToken(userId, UserRole.USER);
    final var claims = Jwts.parser().verifyWith(deriveKey()).build()
      .parseSignedClaims(token).getPayload();

    assertThat(token.split("\\.")).as("JWT should have three segments").hasSize(3);
    assertThat(claims.getSubject()).as("subject should be the user id").isEqualTo(userId.value().toString());
    assertThat(claims.get("role", String.class)).as("role claim should be embedded").isEqualTo("USER");
    assertThat(claims.getExpiration())
      .as("expiration claim should be 30 minutes after issuance")
      .isEqualTo(Date.from(FIXED_NOW.plusMillis(EXPIRATION_MS)));
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
      .isInstanceOf(InvalidTokenException.class);
  }

  @Test
  void shouldRejectExpiredToken() {
    final var expired = Jwts.builder()
      .subject(userId.value().toString())
      .claim("role", UserRole.USER.name())
      .expiration(Date.from(PAST_INSTANT))
      .signWith(deriveKey())
      .compact();

    assertThatThrownBy(() -> provider.validateToken(expired))
      .isInstanceOf(InvalidTokenException.class)
      .hasMessage("Invalid or expired token");
  }

  private static Stream<Arguments> invalidSecretScenarios() {
    return Stream.of(
      arguments(named("shorter than 32 characters", "too-short")),
      arguments(named("blank", "   "))
    );
  }

  @ParameterizedTest(name = "{0} secret is rejected")
  @MethodSource("invalidSecretScenarios")
  void shouldRejectInvalidSecret(final String secret) {
    // Given
    final var clock = Clock.systemUTC();

    // When, then
    assertThatThrownBy(() -> new JwtProvider(secret, EXPIRATION_MS, clock))
      .as("an invalid jwt.secret must fail construction")
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("jwt.secret must be set to at least 32 characters");
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
