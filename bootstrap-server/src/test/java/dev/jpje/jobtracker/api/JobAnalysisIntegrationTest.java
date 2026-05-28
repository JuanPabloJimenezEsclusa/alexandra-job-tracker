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

  private final RestTemplate rest = new RestTemplate();

  @LocalServerPort
  private int port;

  @Test
  void shouldAnalyzeJobPosting() {
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
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { analyzeJobPosting(jobPostingId: \\"%s\\") { summary skills fitScore } }"}
        """.formatted(postingId), jsonHeaders()), String.class);
    assertThat(response.getBody()).contains("summary").contains("skills").contains("fitScore");
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
      """.formatted("analysis-user");
    final var response = rest.exchange(url(), HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()), String.class);
    final var node = new ObjectMapper().readTree(response.getBody());
    return node.findValue("token").asString();
  }
}
