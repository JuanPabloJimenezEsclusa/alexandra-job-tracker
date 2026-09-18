package dev.jpje.jobtracker.auth.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SpringPasswordEncoderTest {

  private static final String SEED_HASH = "$2a$10$jWl7BKHc.pVGpWTOHrIKqer5bzhR.aPra/S/R6j55GRa07Pqw0zJG";

  private final SpringPasswordEncoder encoder = new SpringPasswordEncoder(new BCryptPasswordEncoder(10));

  @Test
  void shouldEncodeWithBcryptCost10() {
    final var hash = encoder.encode("secret");

    assertThat(hash).as("bcrypt cost 10 hashes use the $2a$ prefix").startsWith("$2a$10$");
    assertThat(encoder.matches("secret", hash)).isTrue();
    assertThat(encoder.matches("wrong", hash)).isFalse();
  }

  @Test
  void shouldVerifyJbcryptSeedHash() {
    assertThat(encoder.matches("password123", SEED_HASH))
      .as("existing jbcrypt $2a$ hashes must keep verifying")
      .isTrue();
  }
}
