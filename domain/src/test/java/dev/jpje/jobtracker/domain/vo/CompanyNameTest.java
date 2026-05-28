package dev.jpje.jobtracker.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CompanyNameTest {

  private static final String OVERLONG_NAME = "a".repeat(201);

  private static Stream<Arguments> validNames() {
    return Stream.of(
      arguments(named("regular name", "Acme"), "Acme"),
      arguments(named("name with spaces", "OpenAI Inc."), "OpenAI Inc.")
    );
  }

  private static Stream<Arguments> invalidNames() {
    return Stream.of(
      arguments(named("null", null), NullPointerException.class),
      arguments(named("blank", " "), IllegalArgumentException.class),
      arguments(named("overlong", OVERLONG_NAME), IllegalArgumentException.class)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validNames")
  void shouldCreateValidName(final String value, final String expected) {
    assertThat(CompanyName.of(value)).extracting(CompanyName::value).isEqualTo(expected);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidNames")
  void shouldRejectInvalidNames(final String value, final Class<? extends RuntimeException> exceptionType) {
    assertThatThrownBy(() -> CompanyName.of(value)).isInstanceOf(exceptionType);
  }
}
