package dev.jpje.jobtracker.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UrlTest {

  private static Stream<Arguments> validUrls() {
    return Stream.of(
      arguments(named("https url", "https://example.com/job"), "https://example.com/job"),
      arguments(named("http url", "http://localhost:8080/api"), "http://localhost:8080/api")
    );
  }

  private static Stream<Arguments> invalidUrls() {
    return Stream.of(
      arguments(named("null", null), NullPointerException.class),
      arguments(named("blank", " "), IllegalArgumentException.class),
      arguments(named("malformed", "not a url"), IllegalArgumentException.class),
      arguments(named("non-http protocol", "unknownscheme://example.com"), IllegalArgumentException.class)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validUrls")
  void shouldCreateValidUrl(final String value, final String expected) {
    assertThat(Url.of(value)).extracting(Url::value).isEqualTo(expected);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidUrls")
  void shouldRejectInvalidUrls(final String value, final Class<? extends RuntimeException> exceptionType) {
    assertThatThrownBy(() -> Url.of(value)).isInstanceOf(exceptionType);
  }
}
