package dev.jpje.jobtracker.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import javax.crypto.SecretKey;

import dev.jpje.jobtracker.domain.port.out.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider implements TokenGeneratorPort {
  private final SecretKey key;
  private final long expirationMs;
  private final Clock clock;

  public JwtProvider(@Value("${jwt.secret}") final String secret,
                     @Value("${jwt.expiration:86400000}") final long expirationMs,
                     final Clock clock) {
    this.key = deriveKey(secret);
    this.expirationMs = expirationMs;
    this.clock = clock;
  }

  private static SecretKey deriveKey(final String secret) {
    try {
      final var digest = MessageDigest.getInstance("SHA-512");
      final var hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
      return Keys.hmacShaKeyFor(hash);
    } catch (final NoSuchAlgorithmException e) {
      throw new KeyDerivationException("SHA-512 not available", e);
    }
  }

  @Override
  public String generateToken(final UserId userId) {
    final var now = clock.instant();
    final var expirationTime = now.plusMillis(expirationMs);

    return Jwts.builder()
      .subject(userId.value().toString())
      .claim(Claims.ISSUED_AT, now.getEpochSecond())
      .claim(Claims.EXPIRATION, expirationTime.getEpochSecond())
      .signWith(key)
      .compact();
  }

  public UserId validateToken(final String token) {
    final var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    return new UserId(java.util.UUID.fromString(claims.getPayload().getSubject()));
  }
}
