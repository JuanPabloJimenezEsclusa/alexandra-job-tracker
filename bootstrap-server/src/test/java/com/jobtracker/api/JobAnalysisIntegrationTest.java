package com.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobtracker.api.config.IntegrationTestConfig;
import com.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class JobAnalysisIntegrationTest {

  private final RestTemplate rest = new RestTemplate();

  @LocalServerPort
  private int port;

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
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>(body, jsonHeaders()), String.class);
    assert response.getBody() != null;
    return response.getBody().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
  }

  @Test
  void shouldAnalyzeJobPosting() {
    final var token = registerAndGetToken();
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    final var submitBody = """
      {"query":"mutation($i:SubmitJobInput!){submitJobPosting(input:$i){id}}",\
      "variables":{"i":{"url":"https://example.com/job","title":"Engineer","company":"Acme","description":"Software engineer role","source":"LINKEDIN"}}}
      """;
    final var submitResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>(submitBody, headers), String.class);
    assert submitResp.getBody() != null;
    final var postingId = submitResp.getBody().replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { analyzeJobPosting(jobPostingId: \\"%s\\") { summary skills fitScore } }"}
        """.formatted(postingId), jsonHeaders()), String.class);
    assertThat(response.getBody()).contains("summary").contains("skills").contains("fitScore");
  }
}
