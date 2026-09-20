package dev.jpje.jobtracker.auth.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class SpringPasswordEncoderTest {

  private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(10);

  private final SpringPasswordEncoder encoder = new SpringPasswordEncoder(bcrypt);

  @Test
  void shouldEncodeWithBcryptCost10() {
    final var hash = encoder.encode("secret");

    assertThat(hash).as("bcrypt cost 10 hashes use the $2a$ prefix").startsWith("$2a$10$");
    assertThat(bcrypt.matches("secret", hash)).isTrue();
    assertThat(bcrypt.matches("wrong", hash)).isFalse();
  }
}
