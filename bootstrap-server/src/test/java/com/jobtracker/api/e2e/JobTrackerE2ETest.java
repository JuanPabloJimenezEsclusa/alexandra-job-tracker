package com.jobtracker.api.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Tag("E2ETest")
class JobTrackerE2ETest {

  @LocalServerPort
  private int port;

  private final RestTemplate rest = new RestTemplate();

  private String url() {
    return "http://localhost:%s/graphql".formatted(port);
  }

  private HttpHeaders jsonHeaders() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  @ParameterizedTest(name = "register user {0}, login, verify token")
  @CsvSource({
    "testuser, pass123",
    "e2e_user, secret42"
  })
  void shouldRegisterAndLogin(String username, String password) {
    // Given
    var registerBody = """
      {"query": "mutation { register(username: \\\"%s\\\", password: \\\"%s\\\") { token user { username } } }"}
      """.formatted(username, password);

    // When
    var registerResponse = rest.postForEntity(url(), new HttpEntity<>(registerBody, jsonHeaders()), String.class);

    // Then
    assertThat(registerResponse.getStatusCode().value()).isBetween(200, 201);
    assertThat(registerResponse.getBody()).contains(username);

    // Given
    var loginBody = """
      {"query": "mutation { login(username: \\\"%s\\\", password: \\\"%s\\\") { token } }"}
      """.formatted(username, password);

    // When
    var loginResponse = rest.postForEntity(url(), new HttpEntity<>(loginBody, jsonHeaders()), String.class);

    // Then
    assertThat(loginResponse.getStatusCode().value()).isBetween(200, 201);
    assertThat(loginResponse.getBody()).contains("token");
  }

  @ParameterizedTest(name = "create and list application for user {0}")
  @CsvSource({ "test, test" })
  void shouldCreateAndListApplications(String username, String password) {
    // Given
    var loginBody = """
      {"query": "mutation { register(username: \\\"%s\\\", password: \\\"%s\\\") { token } }"}
      """.formatted(username, password);
    var headers = jsonHeaders();
    var loginResp = rest.postForEntity(url(), new HttpEntity<>(loginBody, headers), String.class);
    assertThat(loginResp.getBody()).isNotNull();
    var token = loginResp.getBody().replaceAll(".*\"token\":\"([^\"]+)\".*", "$1");

    // When
    headers.setBearerAuth(token);
    var createBody = """
      {"query": "mutation { createApplication(company: \\"Acme\\", role: \\"SWE\\", source: LINKEDIN) { id status } }"}
      """;
    var createResp = rest.postForEntity(url(), new HttpEntity<>(createBody, headers), String.class);

    // Then
    assertThat(createResp.getBody()).contains("SAVED");

    // When
    var listBody = """
      {"query": "{ applications { id company role status } }"}
      """;
    var listResp = rest.postForEntity(url(), new HttpEntity<>(listBody, headers), String.class);

    // Then
    assertThat(listResp.getBody()).contains("Acme");
  }
}
