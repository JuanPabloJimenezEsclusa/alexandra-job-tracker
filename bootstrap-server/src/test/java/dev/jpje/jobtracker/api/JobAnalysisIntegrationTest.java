package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class JobAnalysisIntegrationTest {

  private static final String MOCKED_SUMMARY = "Mocked analysis";
  private static final double MOCKED_FIT_SCORE = 85.0;
  private static final String MOCKED_COMPANY_TYPE = "enterprise";
  private static final String MOCKED_SALARY_CURRENCY = "USD";
  private static final String USERNAME = "analysis-user";

  private final RestTemplate rest = new RestTemplate();

  @LocalServerPort
  private int port;

  @Test
  void shouldAnalyzeAndRetrieveJobPosting() {
    final var token = registerAndGetToken();
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    final var submitBody = """
      {"query":"mutation($i:SubmitJobInput!){submitJobPosting(input:$i){id}}",\
      "variables":{"i":{"url":"https://example.com/job","title":"Engineer","company":"Acme","description":"Software engineer role","source":"LINKEDIN"}}}
      """;
    final var submitResp = rest.exchange(url(), HttpMethod.POST, new HttpEntity<>(submitBody, headers), String.class);
    final var node = new ObjectMapper().readTree(submitResp.getBody());
    final var postingId = node.findValue("id").asString();
    final var query = "{\"query\": \"mutation { analyzeJobPosting(jobPostingId: \\\"%s\\\") { "
      + "id summary seniority softSkills technicalSkills fitScore companyRating companyType "
      + "salaryMin salaryMax salaryCurrency } }\", \"variables\": {}}";
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>(query.formatted(postingId), headers), String.class);
    final var analysis = new ObjectMapper().readTree(response.getBody());
    final var analysisId = analysis.findValue("id").asString();
    assertThat(analysis.findValue("summary").asString()).as("analyzed summary").isEqualTo(MOCKED_SUMMARY);
    assertThat(analysis.findValue("fitScore").asDouble()).as("analyzed fit score").isEqualTo(MOCKED_FIT_SCORE);
    assertThat(analysis.findValue("companyType").asString()).as("analyzed company type")
      .isEqualTo(MOCKED_COMPANY_TYPE);
    assertThat(analysis.findValue("salaryCurrency").asString()).as("analyzed salary currency")
      .isEqualTo(MOCKED_SALARY_CURRENCY);

    final var listResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "query { analyses { id jobPostingId summary fitScore } }"}
        """, headers), String.class);
    assertThat(listResp.getBody()).as("saved analysis listed")
      .contains(analysisId).contains(MOCKED_SUMMARY);

    final var deleteResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { deleteAnalysis(id: \\"%s\\") }"}
        """.formatted(analysisId), headers), String.class);
    assertThat(deleteResp.getBody()).as("delete mutation result").contains("true");

    final var afterDelete = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "query { analyses { id } }"}
        """, headers), String.class);
    assertThat(afterDelete.getBody()).as("deleted analysis absent from list").doesNotContain(analysisId);
  }

  private String url() {
    return "http://localhost:%s/api/graphql".formatted(port);
  }

  private HttpHeaders jsonHeaders() {
    final var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  private String registerAndGetToken() {
    final var body = """
      {"query": "mutation { register(username: \\"%s\\", password: \\"pass\\") { token } }"}
      """.formatted(USERNAME);
    final var response = rest.exchange(url(), HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()), String.class);
    final var node = new ObjectMapper().readTree(response.getBody());
    return node.findValue("token").asString();
  }
}
