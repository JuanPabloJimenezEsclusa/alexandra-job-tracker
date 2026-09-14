package dev.jpje.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UserTest {

  private static Stream<Arguments> invalidInputs() {
    final var id = new UserId(UUID.randomUUID());
    final var now = Instant.EPOCH;
    return Stream.of(
      arguments(named("null id", null), Username.of("alice"), "hash", UserRole.USER, now),
      arguments(named("null role", id), Username.of("alice"), "hash", null, now),
      arguments(named("null createdAt", id), Username.of("alice"), "hash", UserRole.USER, null),
      arguments(named("blank passwordHash", id), Username.of("alice"), "", UserRole.USER, now)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidInputs")
  void shouldRejectInvalidInputs(final UserId id, final Username username,
                                  final String passwordHash, final UserRole role,
                                  final Instant createdAt) {
    assertThatThrownBy(() -> new User(id, username, passwordHash, role, createdAt))
      .isInstanceOf(RuntimeException.class);
  }

  @Test
  void shouldCreateValidUser() {
    // Given
    final var id = new UserId(UUID.randomUUID());
    final var now = Instant.EPOCH;

    // When, then
    assertThat(new User(id, Username.of("alice"), "hash", UserRole.USER, now))
      .as("user id").returns(id, User::id)
      .as("username").returns(Username.of("alice"), User::username)
      .as("password hash").returns("hash", User::passwordHash)
      .as("role").returns(UserRole.USER, User::role)
      .as("created at").returns(now, User::createdAt);
  }
}
