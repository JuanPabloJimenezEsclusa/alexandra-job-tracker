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
class AnalyticsIntegrationTest {

  private final RestTemplate rest = new RestTemplate();

  @LocalServerPort
  private int port;

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
    final var response = rest.exchange(url(), HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()), String.class);
    final var node = new ObjectMapper().readTree(response.getBody());
    return node.findValue("token").asString();
  }
}
