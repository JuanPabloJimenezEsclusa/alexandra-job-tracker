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
  private static final String ANALYZE_QUERY = """
    {"query": "mutation { analyzeJobPosting(jobPostingId: \\"%s\\") { id summary seniority softSkills \
    technicalSkills fitScore companyRating companyType salaryMin salaryMax salaryCurrency } }"}
    """;

  @Test
  void shouldAnalyzeJobPosting() {
    final var headers = authHeaders("analyze-user");
    final var postingId = submitPostingAndGetId(headers);
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
  void shouldPersistListenerAnalysisForSubmittedPosting() {
    final var headers = authHeaders("listener-analysis-user");
    final var postingId = submitPostingAndGetId(headers);
    awaitListenerAnalysis(postingId, headers);

    final var analyses = graphql(headers, """
      {"query": "query { analyses { id summary } }"}
      """);
    assertThat(analyses.findValues("summary")).as("listener analysis summary persisted")
      .extracting(JsonNode::asString).contains(MOCKED_SUMMARY);
  }

  @Test
  void shouldDeleteAnalysis() {
    final var headers = authHeaders("delete-analysis-user");
    final var postingId = submitPostingAndGetId(headers);
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
    final var postingId = submitPostingAndGetId(headers);
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

  @Test
  void shouldRejectAnalysisOfAnotherUser() {
    final var ownerHeaders = authHeaders("analysis-owner-user");
    final var postingId = submitPostingAndGetId(ownerHeaders);
    final var analysisId = awaitListenerAnalysis(postingId, ownerHeaders);

    final var otherUserHeaders = authHeaders("analysis-intruder-user");
    final var fetched = graphql(otherUserHeaders, """
      {"query": "query { analysis(id: \\"%s\\") { id summary } }"}
      """.formatted(analysisId));
    assertThat(fetched.findValue("message").asString()).as("cross-user analysis rejected")
      .isEqualTo("Analysis not found");
  }

  @Test
  void shouldRejectMissingAnalysis() {
    final var headers = authHeaders("missing-analysis-user");
    final var fetched = graphql(headers, """
      {"query": "query { analysis(id: \\"00000000-0000-0000-0000-000000000000\\") { id summary } }"}
      """);
    assertThat(fetched.findValue("message").asString()).as("missing analysis rejected")
      .isEqualTo("Analysis not found");
  }

  @Test
  void shouldRejectAnalysisWithoutAuthentication() {
    final var ownerHeaders = authHeaders("analysis-unauth-owner-user");
    final var postingId = submitPostingAndGetId(ownerHeaders);
    final var analysisId = awaitListenerAnalysis(postingId, ownerHeaders);

    final var fetched = graphql(jsonHeaders(), """
      {"query": "query { analysis(id: \\"%s\\") { id summary } }"}
      """.formatted(analysisId));
    assertThat(fetched.findValue("message").asString()).as("analysis without auth rejected")
      .isEqualTo("Authentication required");
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
      analysisId.set(Objects.requireNonNull(analyses.findValues("id").get(index).asString(),
        "analysis id must not be null"));
    });
    return analysisId.get();
  }
}
