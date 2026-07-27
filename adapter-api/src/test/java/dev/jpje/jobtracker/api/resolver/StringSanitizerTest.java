package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringSanitizerTest {

  private static Stream<Arguments> sanitizeScenarios() {
    return Stream.of(
      arguments("clean text", "clean text"),
      arguments("hello\nworld", "hello\nworld"),
      arguments("hello\r\nworld", "hello\r\nworld"),
      arguments("tab\there", "tab\there"),
      arguments("text\u0000with\u0001controls", "textwithcontrols"),
      arguments("\u0000\u0001\u0002", ""),
      arguments("  spaced  ", "spaced"),
      arguments("  ", ""),
      arguments("café", "café"),
      arguments("", "")
    );
  }

  @ParameterizedTest(name = "sanitize({0}) = {1}")
  @MethodSource("sanitizeScenarios")
  void shouldSanitizeString(final String input, final String expected) {
    assertThat(StringSanitizer.sanitize(input)).isEqualTo(expected);
  }
}
