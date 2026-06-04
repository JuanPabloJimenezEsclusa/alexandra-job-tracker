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
class JobPostingIntegrationTest {

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

  private String registerAndGetToken(String username) {
    final var body = """
      {"query": "mutation { register(username: \\"%s\\", password: \\"pass\\") { token } }"}
      """.formatted(username);
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>(body, jsonHeaders()), String.class);
    assert response.getBody() != null;
    return response.getBody().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
  }

  @Test
  void shouldListJobPostings() {
    final var token = registerAndGetToken("jp-list-user");
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "{ jobPostings { id title company } }"}
        """, headers), String.class);
    assertThat(response.getBody()).contains("\"jobPostings\":[]");
  }

  @Test
  void shouldSubmitJobPosting() {
    final var token = registerAndGetToken("submit-list-user");
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    final var body = """
      {"query":"mutation($i:SubmitJobInput!){submitJobPosting(input:$i){id title company source}}",\
      "variables":{"i":{"url":"https://example.com/job/123","title":"Test Engineer",\
      "description":"No empty","company":"TestCorp","source":"LINKEDIN"}}}
      """;
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>(body, headers), String.class);
    assertThat(response.getBody()).contains("Test Engineer").contains("TestCorp").contains("LINKEDIN");

    final var listResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "{ jobPostings { title company } }"}
        """, headers), String.class);
    assertThat(listResp.getBody()).contains("Test Engineer");
  }

  @Test
  void shouldFilterPostingsBySource() {
    final var token = registerAndGetToken("jp-filter-user");
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    final var submitBody = """
      {"query":"mutation($i:SubmitJobInput!){submitJobPosting(input:$i){id}}",\
      "variables":{"i":{"url":"https://linkedin.com/job/1","title":"LinkedIn Job",\
      "description":"No empty","company":"LinkedCorp","source":"LINKEDIN"}}}
      """;
    rest.exchange(url(), HttpMethod.POST, new HttpEntity<>(submitBody, headers), String.class);

    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "{ jobPostings(source: INDEED) { title } }"}
        """, headers), String.class);
    assertThat(response.getBody()).contains("\"jobPostings\":[]");
  }
}
