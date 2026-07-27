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

class AuthPayloadTest {

  private static Stream<Arguments> invalidInputs() {
    final var user = new User(new UserId(UUID.randomUUID()), "alice", "hash", Instant.EPOCH);
    return Stream.of(
      arguments(named("null token", ""), null, user),
      arguments(named("null user", ""), "token", null)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidInputs")
  void shouldRejectInvalidInputs(final String unused, final String token, final User user) {
    assertThatThrownBy(() -> new AuthPayload(token, user))
      .isInstanceOf(NullPointerException.class);
  }

  @Test
  void shouldCreateValidPayload() {
    // Given
    final var user = new User(new UserId(UUID.randomUUID()), "alice", "hash", Instant.EPOCH);

    // When, then
    assertThat(new AuthPayload("token", user))
      .returns("token", AuthPayload::token)
      .returns(user, AuthPayload::user);
  }
}
