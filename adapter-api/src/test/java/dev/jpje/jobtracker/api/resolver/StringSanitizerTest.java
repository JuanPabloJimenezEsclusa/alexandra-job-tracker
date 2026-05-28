package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringSanitizerTest {

  private static Stream<Arguments> sanitizeScenarios() {
    return Stream.of(
      arguments(named("clean text", "clean text"), "clean text"),
      arguments(named("line feed", "hello\nworld"), "hello\nworld"),
      arguments(named("carriage return", "hello\r\nworld"), "hello\r\nworld"),
      arguments(named("tab", "tab\there"), "tab\there"),
      arguments(named("control chars stripped", "text\u0000with\u0001controls"), "textwithcontrols"),
      arguments(named("only control chars", "\u0000\u0001\u0002"), ""),
      arguments(named("outer spaces trimmed", "  spaced  "), "spaced"),
      arguments(named("blank", "  "), ""),
      arguments(named("unicode preserved", "café"), "café"),
      arguments(named("empty", ""), "")
    );
  }

  @ParameterizedTest(name = "sanitize({0}) = {1}")
  @MethodSource("sanitizeScenarios")
  void shouldSanitizeString(final String input, final String expected) {
    assertThat(StringSanitizer.sanitize(input)).isEqualTo(expected);
  }
}
