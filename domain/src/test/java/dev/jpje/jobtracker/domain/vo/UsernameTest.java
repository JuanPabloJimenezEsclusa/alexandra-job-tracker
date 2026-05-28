package dev.jpje.jobtracker.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UsernameTest {

  private static Stream<Arguments> validUsernames() {
    return Stream.of(
      arguments(named("regular username", "alice"), "alice"),
      arguments(named("username with dots", "bob.dev"), "bob.dev")
    );
  }

  private static Stream<Arguments> invalidUsernames() {
    return Stream.of(
      arguments(named("null", null), NullPointerException.class),
      arguments(named("too short", "ab"), IllegalArgumentException.class),
      arguments(named("invalid characters", "a b"), IllegalArgumentException.class)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validUsernames")
  void shouldCreateValidUsername(final String value, final String expected) {
    assertThat(Username.of(value)).extracting(Username::value).isEqualTo(expected);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidUsernames")
  void shouldRejectInvalidUsernames(final String value, final Class<? extends RuntimeException> exceptionType) {
    assertThatThrownBy(() -> Username.of(value)).isInstanceOf(exceptionType);
  }
}
