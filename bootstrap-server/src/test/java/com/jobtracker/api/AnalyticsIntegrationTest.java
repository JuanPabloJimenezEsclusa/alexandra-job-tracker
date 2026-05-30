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
class AnalyticsIntegrationTest {

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
      {"query": "mutation { register(username: \\"analytics-user\\", password: \\"pass\\") { token } }"}
      """;
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>(body, jsonHeaders()), String.class);
    assert response.getBody() != null;
    return response.getBody().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");
  }

  @Test
  void shouldReturnAnalytics() {
    final var token = registerAndGetToken();
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { createApplication(company: \\"X\\", role: \\"Dev\\", source: LINKEDIN) { id } }"}
        """, headers), String.class);

    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "{ analytics { totalApplications perStatus { saved } } }"}
        """, headers), String.class);
    assertThat(response.getBody()).contains("\"totalApplications\"").contains("\"saved\"");
  }
}
