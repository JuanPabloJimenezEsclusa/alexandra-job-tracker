package dev.jpje.jobtracker.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobTitleTest {

  private static final String OVERLONG_TITLE = "a".repeat(201);

  private static Stream<Arguments> validTitles() {
    return Stream.of(
      arguments(named("regular title", "Backend Engineer"), "Backend Engineer"),
      arguments(named("short title", "SRE"), "SRE")
    );
  }

  private static Stream<Arguments> invalidTitles() {
    return Stream.of(
      arguments(named("null", null), NullPointerException.class),
      arguments(named("blank", " "), IllegalArgumentException.class),
      arguments(named("overlong", OVERLONG_TITLE), IllegalArgumentException.class)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validTitles")
  void shouldCreateValidTitle(final String value, final String expected) {
    assertThat(JobTitle.of(value)).extracting(JobTitle::value).isEqualTo(expected);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidTitles")
  void shouldRejectInvalidTitles(final String value, final Class<? extends RuntimeException> exceptionType) {
    assertThatThrownBy(() -> JobTitle.of(value)).isInstanceOf(exceptionType);
  }
}
