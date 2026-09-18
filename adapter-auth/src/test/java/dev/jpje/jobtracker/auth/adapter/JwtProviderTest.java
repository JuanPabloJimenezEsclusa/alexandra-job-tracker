package dev.jpje.jobtracker.auth.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import dev.jpje.jobtracker.auth.crypto.JwtKeyMaterial;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

class JwtProviderTest {

  private static final String SECRET = "super-secret-signing-key-for-tests";
  private static final long EXPIRATION_MS = 1_800_000L;
  private static final Instant FIXED_NOW = Instant.parse("2099-01-01T00:00:00Z");

  private final UserId userId = UserId.generate();
  private final JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(JwtKeyMaterial.derive(SECRET)));
  private final JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(JwtKeyMaterial.derive(SECRET))
    .macAlgorithm(MacAlgorithm.HS512).build();
  private final JwtProvider provider = new JwtProvider(encoder, EXPIRATION_MS,
    Clock.fixed(FIXED_NOW, ZoneOffset.UTC));

  @Test
  void shouldGenerateWellFormedToken() {
    final var token = provider.generateToken(userId, UserRole.USER);
    final var jwt = decoder.decode(token);

    assertThat(token.split("\\.")).as("JWT should have three segments").hasSize(3);
    assertThat(jwt.getSubject()).as("subject should be the user id").isEqualTo(userId.value().toString());
    assertThat(jwt.getClaimAsString("role")).as("role claim should be embedded").isEqualTo("USER");
    assertThat(jwt.getExpiresAt())
      .as("expiration claim should be 30 minutes after issuance")
      .isEqualTo(FIXED_NOW.plusMillis(EXPIRATION_MS));
  }

  @Test
  void shouldEmbedAdminRole() {
    final var token = provider.generateToken(userId, UserRole.ADMIN);

    assertThat(decoder.decode(token).getClaimAsString("role")).isEqualTo("ADMIN");
  }
}
