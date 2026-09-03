package dev.jpje.jobtracker.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import javax.crypto.SecretKey;

import dev.jpje.jobtracker.domain.exception.InvalidTokenException;
import dev.jpje.jobtracker.domain.port.out.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.vo.TokenPayload;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider implements TokenGeneratorPort {
  private static final int MIN_SECRET_LENGTH = 32;

  private final SecretKey key;
  private final long expirationMs;
  private final Clock clock;

  public JwtProvider(@Value("${jwt.secret}") @Nullable final String secret,
                     @Value("${jwt.expiration:1800000}") final long expirationMs,
                     final Clock clock) {
    if (secret == null || secret.isBlank() || secret.length() < MIN_SECRET_LENGTH) {
      throw new IllegalArgumentException("jwt.secret must be set to at least 32 characters");
    }
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
  public String generateToken(final UserId userId, final UserRole role) {
    final var now = clock.instant();
    final var expirationTime = now.plusMillis(expirationMs);

    return Jwts.builder()
      .subject(userId.value().toString())
      .claim(Claims.ISSUED_AT, now.getEpochSecond())
      .claim(Claims.EXPIRATION, expirationTime.getEpochSecond())
      .claim("role", role.name())
      .signWith(key)
      .compact();
  }

  @Override
  public TokenPayload validateToken(final String token) {
    try {
      final var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
      final var userId = new UserId(java.util.UUID.fromString(claims.getSubject()));
      final var roleClaim = claims.get("role", String.class);
      return new TokenPayload(userId, UserRole.valueOf(roleClaim));
    } catch (final JwtException e) {
      throw new InvalidTokenException("Invalid or expired token", e);
    }
  }
}
