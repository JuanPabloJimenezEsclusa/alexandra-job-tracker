package dev.jpje.jobtracker.cli.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JqProcessorTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final JqProcessor processor = new JqProcessor();

  private static Stream<Arguments> scenarios() {
    final var input = json("{\"a\":{\"b\":2}}");
    return Stream.of(
      arguments(named("no expression", null), input.toPrettyString()),
      arguments(named("blank expression", " "), input.toPrettyString()),
      arguments(named("applies expression", ".a.b"), "2"),
      arguments(named("no results", ".[] | select(.a == 99)"), "")
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  void shouldProcessExpression(final String expression, final String expected) {
    assertThat(processor.process(json("{\"a\":{\"b\":2}}"), expression)).isEqualTo(expected);
  }

  @Test
  void shouldReportInvalidExpression() {
    assertThat(processor.process(json("{\"a\":1}"), "[[[")).contains("JQ error");
  }

  private static JsonNode json(final String body) {
    try {
      return MAPPER.readTree(body);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
