package dev.jpje.jobtracker.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RoleNameTest {

  private static final String OVERLONG_ROLE = "a".repeat(151);

  private static Stream<Arguments> validRoles() {
    return Stream.of(
      arguments(named("regular role", "Software Engineer"), "Software Engineer"),
      arguments(named("short role", "PM"), "PM")
    );
  }

  private static Stream<Arguments> invalidRoles() {
    return Stream.of(
      arguments(named("null", null), NullPointerException.class),
      arguments(named("blank", " "), IllegalArgumentException.class),
      arguments(named("overlong", OVERLONG_ROLE), IllegalArgumentException.class)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validRoles")
  void shouldCreateValidRole(final String value, final String expected) {
    assertThat(RoleName.of(value)).extracting(RoleName::value).isEqualTo(expected);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidRoles")
  void shouldRejectInvalidRoles(final String value, final Class<? extends RuntimeException> exceptionType) {
    assertThatThrownBy(() -> RoleName.of(value)).isInstanceOf(exceptionType);
  }
}
