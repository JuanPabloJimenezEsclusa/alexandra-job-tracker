package dev.jpje.jobtracker.bootstrap.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobPostingCommandsIntegrationTest extends BaseCliIntegrationTest {
  private static final String JOB_POSTINGS_RESPONSE = """
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
  """;


  private static Stream<Arguments> scenarios() {
    return Stream.of(
      arguments(named("list postings with data", "jobPostings"),
        JOB_POSTINGS_RESPONSE,
        "po",
        "Engineer"),
      arguments(named("list postings with source filter", "jobPostings"),
        JOB_POSTINGS_RESPONSE,
        "po -s LINKEDIN",
        "Engineer"),
      arguments(named("list postings error", "jobPostings"),
        """
          {"errors": [{"message": "List failed"}], "data": null}
        """,
        "po",
        "List failed"),
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
        "Engineer"),
      arguments(named("submit job posting null data", "submitJobPosting"),
        """
          {"data": {"submitJobPosting": null}}
        """,
        "sj -u https://example.com/job -t Engineer -d 'Desc' -c Acme -s LINKEDIN",
        "null"),
      arguments(named("analyze job posting", "analyzeJobPosting"),
        """
          {
            "data": {
              "analyzeJobPosting": {
                "id": "a1111111-1111-1111-1111-111111111111",
                "jobPostingId": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
                "summary": "Java role", "seniority": "mid", "softSkills": [], "technicalSkills": ["Java"], "fitScore": 85.0,
                "companyRating": 4.2, "companyType": "enterprise", "salaryMin": 90000.0, "salaryMax": 130000.0, "salaryCurrency": "USD",
                "createdAt": "2026-01-01T00:00:00Z"
              }
            }
          }
          """,
        "anlz -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
        "85.0"),
      arguments(named("analyze job posting error", "analyzeJobPosting"),
        """
          {"errors": [{"message": "Analyze failed"}], "data": null}
          """,
        "anlz -i b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
        "Analyze failed"),
      arguments(named("list analyses with data", "analyses"),
        """
          {
            "data": {
              "analyses": [{
                "id": "a1111111-1111-1111-1111-111111111111",
                "jobPostingId": "b6124fbc-eaba-4f38-bea5-54bbd88fe19a",
                "summary": "Java role",
                "seniority": "mid",
                "softSkills": ["Teamwork"],
                "technicalSkills": ["Java"],
                "fitScore": 85.0,
                "companyType": "enterprise",
                "salaryMin": 90000.0,
                "salaryMax": 130000.0,
                "salaryCurrency": "USD",
                "createdAt": "2026-01-01T00:00:00Z"
              }]
            }
          }
          """,
        "al",
        "Java role"),
      arguments(named("delete analysis", "deleteAnalysis"),
        """
          {
            "data": {
              "deleteAnalysis": true
            }
          }
          """,
        "dal -i a1111111-1111-1111-1111-111111111111",
        "Deleted analysis: a1111111-1111-1111-1111-111111111111")
    );
  }

  private static Stream<Arguments> negativeScenarios() {
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
        "Error")
    );
  }

  @BeforeEach
  void setUp() throws Exception {
    authenticate();
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
