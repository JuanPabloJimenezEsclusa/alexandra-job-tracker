package dev.jpje.jobtracker.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobPostingCommandsIntegrationTest extends BaseCliIntegrationTest {

  private static Stream<Arguments> scenarios() {
    return Stream.of(
      arguments(named("list postings", "jobPostings"),
        """
          {
            "data": {
              "jobPostings": []
            }
          }
        """,
        "po",
        "Error",
        false),
      arguments(named("list postings with data", "jobPostings"),
        """
          {
            "data": {
              "jobPostings": [{
                "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
                "title": "Engineer",
                "company": "Acme",
                "source": "LINKEDIN",
                "url": "https://example.com/job",
                "description": "Awesome role",
                "postedAt": "2026-01-01T00:00:00Z"
              }]
            }
          }
        """,
        "po",
        "Engineer",
        true),
      arguments(named("list postings with source filter", "jobPostings"),
        """
          {
            "data": {
              "jobPostings": [{
                "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
                "title": "Engineer",
                "company": "Acme",
                "source": "LINKEDIN",
                "url": "https://example.com/job",
                "description": "Awesome role",
                "postedAt": "2026-01-01T00:00:00Z"
              }]
            }
          }
        """,
        "po -s LINKEDIN",
        "Engineer",
        true),
      arguments(named("list postings error", "jobPostings"),
        """
          {"errors": [{"message": "List failed"}], "data": null}
        """,
        "po",
        "List failed",
        true),
      arguments(named("submit job posting", "submitJobPosting"),
        """
          {
            "data": {
              "submitJobPosting": {
                "id": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
                "title": "Engineer",
                "description": "Awesome",
                "company": "Acme",
                "source": "LINKEDIN"
              }
            }
          }
        """,
        "sj -u https://example.com/job -t Engineer -d 'Awesome' -c Acme -s LINKEDIN -s 'Java dev'",
        "Engineer",
        true),
      arguments(named("submit job posting null data", "submitJobPosting"),
        """
          {"data": {"submitJobPosting": null}}
        """,
        "sj -u https://example.com/job -t Engineer -d 'Desc' -c Acme -s LINKEDIN",
        "null",
        true),
      arguments(named("analyze job posting", "analyzeJobPosting"),
        """
          {
            "data": {
              "analyzeJobPosting": {
                "summary": "Java role", "skills": ["Java"], "fitScore": 85.0
              }
            }
          }
          """,
        "anlz -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
        "85.0",
        true),
      arguments(named("analyze job posting error", "analyzeJobPosting"),
        """
          {"errors": [{"message": "Analyze failed"}], "data": null}
          """,
        "anlz -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
        "Analyze failed",
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
