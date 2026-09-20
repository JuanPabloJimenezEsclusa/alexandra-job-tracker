package dev.jpje.jobtracker.auth.adapter;

import java.time.Clock;

import dev.jpje.jobtracker.application.port.outbound.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider implements TokenGeneratorPort {
  private final JwtEncoder encoder;
  private final long expirationMs;
  private final Clock clock;

  public JwtProvider(final JwtEncoder encoder,
                     @Value("${jwt.expiration:1800000}") final long expirationMs,
                     final Clock clock) {
    this.encoder = encoder;
    this.expirationMs = expirationMs;
    this.clock = clock;
  }

  @Override
  public String generateToken(final UserId userId, final UserRole role) {
    final var now = clock.instant();
    final var claims = JwtClaimsSet.builder()
      .subject(userId.value().toString())
      .issuedAt(now)
      .expiresAt(now.plusMillis(expirationMs))
      .claim("role", role.name())
      .build();
    final var header = JwsHeader.with(MacAlgorithm.HS512).build();
    return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
  }
}
