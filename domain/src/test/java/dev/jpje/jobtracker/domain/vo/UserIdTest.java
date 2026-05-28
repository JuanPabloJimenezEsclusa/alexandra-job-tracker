package dev.jpje.jobtracker.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class UserIdTest {

  @Test
  void shouldRejectNullValue() {
    assertThatThrownBy(() -> new UserId(null))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldGenerateRandomId() {
    // When
    final var first = UserId.generate();
    final var second = UserId.generate();

    // Then
    assertThat(first).as("generated ids should be unique").isNotEqualTo(second);
    assertThat(first.value().version()).as("generated id should be a random (v4) UUID").isEqualTo(4);
  }

  @Test
  void shouldWrapGivenValue() {
    // Given
    final var uuid = UUID.randomUUID();

    // When
    final var id = new UserId(uuid);

    // Then
    assertThat(id.value()).isEqualTo(uuid);
  }
}
