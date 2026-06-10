package com.jobtracker.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TrackCommandsIntegrationTest extends BaseCliIntegrationTest {

  private static Stream<Arguments> scenarios() {
    return Stream.of(
      arguments(named("list empty applications", "applications"),
        """
          {
            "data": {
              "applications": []
            }
          }
        """,
        "l",
        "Error",
        false),
      arguments(named("add application", "createApplication"),
        """
          {
            "data": {
              "createApplication": {
                "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a", "status": "SAVED"
              }
            }
          }
        """,
        "a -c Acme -r Engineer -s LINKEDIN",
        "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
        true),
      arguments(named("list applications with data", "applications"),
        """
          {
            "data": {
              "applications": [{
                "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
                "company": "Acme",
                "role": "Engineer",
                "source": "LINKEDIN",
                "status": "APPLIED",
                "dateApplied": "2026-01-01T00:00:00Z",
                "lastUpdated": "2026-01-01T00:00:00Z",
                "postingUrl": null,
                "notes": null
              }]
            }
          }
        """,
        "l",
        "Acme",
        true),
      arguments(named("update application status", "updateApplicationStatus"),
        """
          {
            "data": {
              "updateApplicationStatus": {
                "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
                "status": "INTERVIEWING",
                "lastUpdated": "2026-06-01T00:00:00Z"
              }
            }
          }
        """,
        "u -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s INTERVIEWING",
        "INTERVIEWING",
        true),
      arguments(named("delete application", "deleteApplication"),
        """
          {
            "data": {
              "deleteApplication": true
            }
          }
        """,
        "d -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
        "Deleted",
        true)
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
                            final String expected,
                            final boolean shouldContain) throws Exception {
    stubGraphql(operationName, response);
    final var result = shell.sendCommand(command);
    if (shouldContain) {
      assertThat(result.lines()).anyMatch(line -> line.contains(expected));
    } else {
      assertThat(result.lines()).noneMatch(line -> line.contains(expected));
    }
  }
}
