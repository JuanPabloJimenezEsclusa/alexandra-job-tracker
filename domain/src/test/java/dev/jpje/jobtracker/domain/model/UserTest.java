package dev.jpje.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UserTest {

  private static Stream<Arguments> invalidInputs() {
    final var id = new UserId(UUID.randomUUID());
    final var now = Instant.EPOCH;
    return Stream.of(
      arguments(named("null id", ""), null, "alice", "hash", now),
      arguments(named("null createdAt", ""), id, "alice", "hash", null),
      arguments(named("blank username", ""), id, "", "hash", now),
      arguments(named("blank passwordHash", ""), id, "alice", "", now)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidInputs")
  void shouldRejectInvalidInputs(final String unused, final UserId id, final String username,
                                  final String passwordHash, final Instant createdAt) {
    assertThatThrownBy(() -> new User(id, username, passwordHash, createdAt))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void shouldCreateValidUser() {
    // Given
    final var id = new UserId(UUID.randomUUID());
    final var now = Instant.EPOCH;

    // When, then
    assertThat(new User(id, "alice", "hash", now))
      .returns(id, User::id)
      .returns("alice", User::username)
      .returns("hash", User::passwordHash)
      .returns(now, User::createdAt);
  }
}
