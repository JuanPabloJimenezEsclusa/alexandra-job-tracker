package com.jobtracker.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnalyticsCommandsIntegrationTest extends BaseCliIntegrationTest {

  private static final String ANALYTICS_RESPONSE = """
    {
      "data": {
        "analytics": {
          "totalApplications": 10,
          "conversionRate": 30.0,
          "perStatus": {
            "saved": 3,
            "applied": 4,
            "interviewing": 2,
            "offer": 1,
            "accepted": 0,
            "rejected": 0,
            "withdrawn": 0
          }
        }
      }
    }
    """;

  private static Stream<Arguments> scenarios() {
    return Stream.of(
      arguments(named("default analytics output", "analytics"), ANALYTICS_RESPONSE,
        "an", "Analytics:"),
      arguments(named("analytics with jq filter", "analytics"), ANALYTICS_RESPONSE,
        "an -j .conversionRate", "30.0"),
      arguments(named("analytics with since filter", "analytics"), ANALYTICS_RESPONSE,
        "an -s 2026-01-01", "30.0"),
      arguments(named("analytics error", "analytics"),
        """
          {"errors": [{"message": "Analytics failed"}], "data": null}
        """,
        "an", "Analytics failed")
    );
  }

  @BeforeEach
  void setUp() throws Exception {
    authenticate();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  void shouldExecuteCommand(final String operationName,
                            final String response,
                            final String command,
                            final String expected) throws Exception {
    stubGraphql(operationName, response);
    final var result = shell.sendCommand(command);
    assertThat(result.lines()).anyMatch(line -> line.contains(expected));
  }
}
