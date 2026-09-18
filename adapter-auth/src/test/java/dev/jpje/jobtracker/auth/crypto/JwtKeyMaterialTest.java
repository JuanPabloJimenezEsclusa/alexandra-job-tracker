package dev.jpje.jobtracker.auth.crypto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JwtKeyMaterialTest {

  private static Stream<Arguments> invalidSecrets() {
    return Stream.of(
      arguments(named("shorter than 32 characters", "too-short")),
      arguments(named("blank", "   ")),
      arguments(named("empty", ""))
    );
  }

  @ParameterizedTest(name = "{0} secret is rejected")
  @MethodSource("invalidSecrets")
  void shouldRejectInvalidSecret(final String secret) {
    assertThatThrownBy(() -> JwtKeyMaterial.derive(secret))
      .as("an invalid jwt.secret must fail construction")
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("jwt.secret must be set to at least 32 characters");
  }

  @Test
  void shouldDeriveHmacSha512Key() {
    final var key = JwtKeyMaterial.derive("super-secret-signing-key-for-tests");

    assertThat(key.getAlgorithm()).as("HS512 requires an HmacSHA512 key").isEqualTo("HmacSHA512");
    assertThat(key.getEncoded()).as("SHA-512 digest is 64 bytes").hasSize(64);
  }
}
