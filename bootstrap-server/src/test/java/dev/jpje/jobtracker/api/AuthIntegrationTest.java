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
class AuthIntegrationTest {

  private final RestTemplate rest = new RestTemplate();

  @LocalServerPort
  private int port;

  @Test
  void shouldRegister() {
    final var body = """
      {"query": "mutation { register(username: \\"alice\\", password: \\"pass\\") { token user { username } } }"}
      """;
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>(body, jsonHeaders()), String.class);
    assertThat(response).matches(r ->
      r.getStatusCode().is2xxSuccessful()
        && r.getBody() != null
        && r.getBody().contains("token")
        && r.getBody().contains("alice"));
  }

  @Test
  void shouldRejectDuplicateRegistration() {
    final var body = """
      {"query": "mutation { register(username: \\"bob\\", password: \\"pass\\") { token } }"}
      """;
    rest.exchange(url(), HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()), String.class);
    final var response = rest.exchange(url(), HttpMethod.POST, new HttpEntity<>(body, jsonHeaders()), String.class);
    assertThat(response.getBody()).contains("Username already taken");
  }

  @Test
  void shouldLogin() {
    rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { register(username: \\"carol\\", password: \\"pass\\") { token } }"}
        """, jsonHeaders()), String.class);
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { login(username: \\"carol\\", password: \\"pass\\") { token user { username } } }"}
        """, jsonHeaders()), String.class);
    assertThat(response.getBody()).contains("token").contains("carol");
  }

  @Test
  void shouldRejectInvalidLogin() {
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { login(username: \\"nobody\\", password: \\"wrong\\") { token } }"}
        """, jsonHeaders()), String.class);
    assertThat(response.getBody()).contains("Invalid credentials");
  }

  @Test
  void shouldReturnMe() {
    final var registerResp = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { register(username: \\"dave\\", password: \\"pass\\") { token } }"}
        """, jsonHeaders()), String.class);
    final var node = new ObjectMapper().readTree(registerResp.getBody());
    final var token = node.findValue("token").asString();
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "{ me { username } }"}
        """, headers), String.class);
    assertThat(response.getBody()).contains("dave");
  }

  @Test
  void shouldReturnNullMeWithoutAuth() {
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "{ me { username } }"}
        """, jsonHeaders()), String.class);
    assertThat(response.getBody()).doesNotContain("username");
  }

  @Test
  void shouldLogout() {
    final var response = rest.exchange(url(), HttpMethod.POST,
      new HttpEntity<>("""
        {"query": "mutation { logout }"}
        """, jsonHeaders()), String.class);
    assertThat(response.getBody()).contains("\"logout\":true");
  }

  private String url() {
    return "http://localhost:%s/api/graphql".formatted(port);
  }

  private HttpHeaders jsonHeaders() {
    final var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }
}
