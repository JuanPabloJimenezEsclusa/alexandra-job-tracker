package dev.jpje.jobtracker.cli;

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
      arguments(named("add application with url and notes", "createApplication"),
        """
          {
            "data": {
              "createApplication": {
                "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a", "status": "SAVED"
              }
            }
          }
        """,
        "a -c Acme -r Engineer -s LINKEDIN -u http://example.com -n 'Some notes'",
        "SAVED",
        true),
      arguments(named("add application error", "createApplication"),
        """
          {"errors": [{"message": "Request failed"}], "data": null}
        """,
        "a -c Acme -r Engineer -s LINKEDIN",
        "Request failed",
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
      arguments(named("list with status filter", "applications"),
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
        "l -s APPLIED",
        "Acme",
        true),
      arguments(named("list with source filter", "applications"),
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
        "l --source LINKEDIN",
        "Acme",
        true),
      arguments(named("list applications error", "applications"),
        """
          {"errors": [{"message": "List failed"}], "data": null}
        """,
        "l",
        "List failed",
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
      arguments(named("update application with notes", "updateApplicationStatus"),
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
        "u -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s INTERVIEWING -n 'Followed up'",
        "INTERVIEWING",
        true),
      arguments(named("update application error", "updateApplicationStatus"),
        """
          {"errors": [{"message": "Update failed"}], "data": null}
        """,
        "u -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s INTERVIEWING",
        "Update failed",
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
        true),
      arguments(named("list with jq filter empty result", "applications"),
        """
          {
            "data": {
              "applications": [{
                "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
                "company": "Acme",
                "role": "Engineer",
                "source": "LINKEDIN",
                "status": "APPLIED"
              }]
            }
          }
        """,
        "l -j '.[] | select(.company == \"NONE\")'",
        "Acme",
        false),
      arguments(named("list with invalid jq expression", "applications"),
        """
          {
            "data": {
              "applications": []
            }
          }
        """,
        "l -j '[[['",
        "JQ error",
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
