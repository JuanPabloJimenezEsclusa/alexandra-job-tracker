package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class JobAnalysisIntegrationTest extends GraphQlIntegrationTestBase {

  private static final String MOCKED_SUMMARY = "Mocked analysis";
  private static final double MOCKED_FIT_SCORE = 85.0;
  private static final String MOCKED_COMPANY_TYPE = "enterprise";
  private static final String MOCKED_SALARY_CURRENCY = "USD";
  private static final String SUBMIT_BODY = """
    {"query":"mutation($i:SubmitJobInput!){submitJobPosting(input:$i){id}}",\
    "variables":{"i":{"url":"https://example.com/job","title":"Engineer","company":"Acme","description":"Software engineer role","source":"LINKEDIN"}}}
    """;
  private static final String ANALYZE_QUERY = """
    {"query": "mutation { analyzeJobPosting(jobPostingId: \\"%s\\") { id summary seniority softSkills \
    technicalSkills fitScore companyRating companyType salaryMin salaryMax salaryCurrency } }"}
    """;

  @Test
  void shouldAnalyzeJobPosting() {
    final var headers = authHeaders("analyze-user");
    final var postingId = submitPosting(headers);
    deleteListenerAnalysis(postingId, headers);

    final var analysis = analyzePosting(postingId, headers);
    assertThat(analysis.findValue("summary").asString()).as("analyzed summary").isEqualTo(MOCKED_SUMMARY);
    assertThat(analysis.findValue("fitScore").asDouble()).as("analyzed fit score").isEqualTo(MOCKED_FIT_SCORE);
    assertThat(analysis.findValue("companyType").asString()).as("analyzed company type")
      .isEqualTo(MOCKED_COMPANY_TYPE);
    assertThat(analysis.findValue("salaryCurrency").asString()).as("analyzed salary currency")
      .isEqualTo(MOCKED_SALARY_CURRENCY);

    final var analyses = graphql(headers, """
      {"query": "query { analyses { id jobPostingId summary fitScore } }"}
      """);
    assertThat(analyses.findValues("id")).as("saved analysis listed")
      .extracting(JsonNode::asString).contains(analysis.findValue("id").asString());
    assertThat(analyses.findValues("summary")).as("saved analysis summary")
      .extracting(JsonNode::asString).contains(MOCKED_SUMMARY);
  }

  @Test
  void shouldDeleteAnalysis() {
    final var headers = authHeaders("delete-analysis-user");
    final var postingId = submitPosting(headers);
    final var analysisId = awaitListenerAnalysis(postingId, headers);

    final var deleted = graphql(adminHeaders(), """
      {"query": "mutation { deleteAnalysis(id: \\"%s\\") }"}
      """.formatted(analysisId));
    assertThat(deleted.findValue("deleteAnalysis").asBoolean()).as("delete mutation result").isTrue();

    final var afterDelete = graphql(headers, """
      {"query": "query { analyses { id } }"}
      """);
    assertThat(afterDelete.findValues("id")).as("deleted analysis absent from list")
      .extracting(JsonNode::asString).isEmpty();
  }

  @Test
  void shouldRejectDeleteAnalysisForNonAdmin() {
    final var headers = authHeaders("forbidden-delete-user");
    final var postingId = submitPosting(headers);
    final var analysisId = awaitListenerAnalysis(postingId, headers);

    final var deleted = graphql(headers, """
      {"query": "mutation { deleteAnalysis(id: \\"%s\\") }"}
      """.formatted(analysisId));
    assertThat(deleted.findValue("message").asString()).as("delete analysis as non-admin rejected")
      .isEqualTo("Admin access required");

    final var afterDelete = graphql(headers, """
      {"query": "query { analyses { id } }"}
      """);
    assertThat(afterDelete.findValues("id")).as("analysis kept after forbidden delete")
      .extracting(JsonNode::asString).contains(analysisId);
  }

  private HttpHeaders authHeaders(final String username) {
    final var headers = jsonHeaders();
    headers.setBearerAuth(registerAndGetToken(username));
    return headers;
  }

  private String submitPosting(final HttpHeaders headers) {
    final var submitted = graphql(headers, SUBMIT_BODY);
    return Objects.requireNonNull(submitted.findValue("id"),
      "submit response must contain a posting id").asString();
  }

  private void deleteListenerAnalysis(final String postingId, final HttpHeaders headers) {
    final var analysisId = awaitListenerAnalysis(postingId, headers);
    final var deleted = graphql(adminHeaders(), """
      {"query": "mutation { deleteAnalysis(id: \\"%s\\") }"}
      """.formatted(analysisId));
    assertThat(deleted.findValue("deleteAnalysis").asBoolean()).as("delete listener analysis result").isTrue();
  }

  private JsonNode analyzePosting(final String postingId, final HttpHeaders headers) {
    return graphql(headers, ANALYZE_QUERY.formatted(postingId));
  }

  private String awaitListenerAnalysis(final String postingId, final HttpHeaders headers) {
    final var analysisId = new AtomicReference<String>();
    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      final var analyses = graphql(headers, """
        {"query": "query { analyses { id jobPostingId } }"}
        """);
      final var postingIds = analyses.findValues("jobPostingId");
      assertThat(postingIds)
        .as("async analysis persisted for submitted posting")
        .extracting(JsonNode::asString)
        .contains(postingId);
      final var index = postingIds.stream().map(JsonNode::asString).toList().indexOf(postingId);
      analysisId.set(analyses.findValues("id").get(index).asString());
    });
    return analysisId.get();
  }
}
