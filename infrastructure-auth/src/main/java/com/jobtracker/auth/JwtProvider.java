package com.jobtracker.auth;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.jobtracker.domain.vo.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
  private final SecretKey key;
  private final long expirationMs;

  public JwtProvider(@Value("${jwt.secret}") final String secret,
                     @Value("${jwt.expiration:86400000}") final long expirationMs) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMs = expirationMs;
  }

  public String generateToken(final UserId userId) {
    return Jwts.builder()
      .subject(userId.value().toString())
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + expirationMs))
      .signWith(key)
      .compact();
  }

  public UserId validateToken(final String token) {
    final var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
    return new UserId(java.util.UUID.fromString(claims.getPayload().getSubject()));
  }
}
