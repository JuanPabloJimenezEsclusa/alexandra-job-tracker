package com.jobtracker.domain.vo;

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
    final var id = UserId.generate();

    // Then
    assertThat(id.value()).isNotNull();
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
