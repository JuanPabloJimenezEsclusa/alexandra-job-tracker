package com.jobtracker.auth;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Date;
import javax.crypto.SecretKey;

import com.jobtracker.domain.port.out.TokenGeneratorPort;
import com.jobtracker.domain.vo.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Provides JWT token generation and validation.
 */
@Component
public class JwtProvider implements TokenGeneratorPort {
  private final SecretKey key;
  private final long expirationMs;
  private final Clock clock;

  /**
   * Creates a provider with the configured secret and expiration.
   */
  public JwtProvider(@Value("${jwt.secret}") final String secret,
                     @Value("${jwt.expiration:86400000}") final long expirationMs,
                     final Clock clock) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
    this.clock = clock;
  }

  @SuppressWarnings("java:S2143") // Allow using java.util.Date for JWT claims
  @Override
  public String generateToken(final UserId userId) {
    final var now = clock.instant();
    return Jwts.builder()
      .subject(userId.value().toString())
      .issuedAt(Date.from(now))  //NOSONAR
      .expiration(Date.from(now.plusMillis(expirationMs))) //NOSONAR
      .signWith(key)
      .compact();
  }

  /**
   * Validates a JWT and returns the embedded user ID.
   */
  public UserId validateToken(final String token) {
    final var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    return new UserId(java.util.UUID.fromString(claims.getPayload().getSubject()));
  }
}
