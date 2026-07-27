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

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class ApplicationIntegrationTest {

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
  void shouldCreateAndListApplications() {
    final var token = registerAndGetToken("list-user");
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    final var createResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { createApplication(company: \\"Acme\\", role: \\"SWE\\", source: LINKEDIN) { id status } }"}
        """, headers), String.class);
    assertThat(createResp.getBody()).contains("SAVED");

    final var listResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "{ applications { company role status } }"}
        """, headers), String.class);
    assertThat(listResp.getBody()).contains("Acme");
  }

  @Test
  void shouldCreateAndUpdateAndDelete() {
    final var token = registerAndGetToken("crud-user");
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    final var createResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { createApplication(company: \\"Beta\\", role: \\"PM\\", source: INDEED) { id status } }"}
        """, headers), String.class);
    assertThat(createResp.getBody()).contains("SAVED");
    final var appId = createResp.getBody().replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

    final var updateResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { updateApplicationStatus(id: \\"%s\\", status: INTERVIEWING) { status } }"}
        """.formatted(appId), headers), String.class);
    assertThat(updateResp.getBody()).contains("INTERVIEWING");

    final var deleteResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { deleteApplication(id: \\"%s\\") }"}
        """.formatted(appId), headers), String.class);
    assertThat(deleteResp.getBody()).contains("\"deleteApplication\":true");
  }

  @Test
  void shouldFilterApplicationsByStatus() {
    final var token = registerAndGetToken("filter-user");
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { createApplication(company: \\"Gamma\\", role: \\"Dev\\", source: OTHER) { id } }"}
        """, headers), String.class);

    final var listResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "{ applications(status: APPLIED) { company } }"}
        """, headers), String.class);
    assertThat(listResp.getBody()).doesNotContain("Gamma");
  }
}
