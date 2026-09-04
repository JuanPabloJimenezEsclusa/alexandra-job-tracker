package dev.jpje.jobtracker.bootstrap.cli;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TrackCommandsIntegrationTest extends BaseCliIntegrationTest {
  private static final String CREATE_APPLICATION_RESPONSE = """
    {
      "data": {
        "createApplication": {
          "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a", "jobPostingId": "7c9e6679-7425-40de-944b-e07fc1f90ae7", "status": "SAVED"
        }
      }
    }
  """;

  private static final String LIST_APPLICATIONS_RESPONSE = """
    {
      "data": {
        "applications": [{
          "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
          "jobPostingId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
          "status": "APPLIED",
          "dateApplied": "2026-01-01T00:00:00Z",
          "lastUpdated": "2026-01-01T00:00:00Z",
          "notes": null
        }]
      }
    }
  """;

  private static final String UPDATE_APPLICATION_STATUS_RESPONSE = """
    {
      "data": {
        "updateApplicationStatus": {
          "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
          "status": "INTERVIEWING",
          "lastUpdated": "2026-06-01T00:00:00Z"
        }
      }
    }
  """;

  private static final String EMPTY_APPLICATIONS_RESPONSE = """
    {
      "data": {
        "applications": []
      }
    }
  """;


  private static Stream<Arguments> scenarios() {
    return Stream.of(
      arguments(named("add application", "createApplication"),
        CREATE_APPLICATION_RESPONSE,
        "a -i 7c9e6679-7425-40de-944b-e07fc1f90ae7",
        "b6124fbc-eaba-4f38-bea5-54bbd88fe19a"),
      arguments(named("add application with notes", "createApplication"),
        CREATE_APPLICATION_RESPONSE,
        "a -i 7c9e6679-7425-40de-944b-e07fc1f90ae7 -n 'Some notes'",
        "SAVED"),
      arguments(named("add application error", "createApplication"),
        """
          {"errors": [{"message": "Request failed"}], "data": null}
        """,
        "a -i 7c9e6679-7425-40de-944b-e07fc1f90ae7",
        "Request failed"),
      arguments(named("list applications with data", "applications"),
        LIST_APPLICATIONS_RESPONSE,
        "l",
        "b6124fbc-eaba-4f38-bea5-54bbd88fe19a"),
      arguments(named("list with status filter", "applications"),
        LIST_APPLICATIONS_RESPONSE,
        "l -s APPLIED",
        "b6124fbc-eaba-4f38-bea5-54bbd88fe19a"),
      arguments(named("list applications error", "applications"),
        """
          {"errors": [{"message": "List failed"}], "data": null}
        """,
        "l",
        "List failed"),
      arguments(named("update application status", "updateApplicationStatus"),
        UPDATE_APPLICATION_STATUS_RESPONSE,
        "u -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s INTERVIEWING",
        "INTERVIEWING"),
      arguments(named("update application with notes", "updateApplicationStatus"),
        UPDATE_APPLICATION_STATUS_RESPONSE,
        "u -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s INTERVIEWING -n 'Followed up'",
        "INTERVIEWING"),
      arguments(named("update application error", "updateApplicationStatus"),
        """
          {"errors": [{"message": "Update failed"}], "data": null}
        """,
        "u -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a -s INTERVIEWING",
        "Update failed"),
      arguments(named("delete application", "deleteApplication"),
        """
          {
            "data": {
              "deleteApplication": true
            }
          }
        """,
        "d -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
        "Deleted"),
      arguments(named("list with invalid jq expression", "applications"),
        EMPTY_APPLICATIONS_RESPONSE,
        "l -j '[[['",
        "JQ error")
    );
  }

  private static Stream<Arguments> negativeScenarios() {
    return Stream.of(
      arguments(named("list empty applications", "applications"),
        EMPTY_APPLICATIONS_RESPONSE,
        "l",
        "Error"),
      arguments(named("list with jq filter empty result", "applications"),
        """
          {
            "data": {
              "applications": [{
                "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
                "jobPostingId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                "status": "APPLIED"
              }]
            }
          }
        """,
        "l -j '.[] | select(.status == \"NONE\")'",
        "b6124fbc-eaba-4f38-bea5-54bbd88fe19a")
    );
  }

  @BeforeEach
  void setUp() throws Exception {
    authenticate();
  }

  @Test
  void shouldSendApplicationsQueryWithoutSourceArgument() {
    stubGraphql("applications", LIST_APPLICATIONS_RESPONSE);
    sendCommandUnchecked("l -s APPLIED");

    verify(0, postRequestedFor(urlPathEqualTo("/api/graphql"))
      .withRequestBody(containing("\"source\"")));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  void shouldContainExpectedOutput(final String operationName,
                                   final String response,
                                   final String command,
                                   final String expected) {
    stubGraphql(operationName, response);
    final var result = sendCommandUnchecked(command);
    assertThat(result.lines()).anyMatch(line -> line.contains(expected));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("negativeScenarios")
  void shouldNotContainUnexpectedOutput(final String operationName,
                                        final String response,
                                        final String command,
                                        final String unexpected) {
    stubGraphql(operationName, response);
    final var result = sendCommandUnchecked(command);
    assertThat(result.lines()).noneMatch(line -> line.contains(unexpected));
  }
}
