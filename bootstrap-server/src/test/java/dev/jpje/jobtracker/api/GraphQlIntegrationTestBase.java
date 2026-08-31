package dev.jpje.jobtracker.api;

import java.util.Objects;

import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public abstract class GraphQlIntegrationTestBase {

  protected final RestClient rest = RestClient.builder().build();
  protected final ObjectMapper mapper = new ObjectMapper();

  @LocalServerPort
  private int port;

  protected String url() {
    return "http://localhost:%s/api/graphql".formatted(port);
  }

  protected HttpHeaders jsonHeaders() {
    final var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  protected HttpHeaders authHeaders(final String username) {
    final var headers = jsonHeaders();
    headers.setBearerAuth(registerAndGetToken(username));
    return headers;
  }

  protected String registerAndGetToken(final String username) {
    final var body = """
      {"query": "mutation { register(username: \\"%s\\", password: \\"pass\\", role: USER) { token } }"}
      """.formatted(username);
    final var registration = graphql(adminHeaders(), body);
    return Objects.requireNonNull(registration.findValue("token"),
      "register response must contain a token").asString();
  }

  protected HttpHeaders adminHeaders() {
    final var body = """
      {"query": "mutation { login(username: \\"alexandra\\", password: \\"password123\\") { token } }"}
      """;
    final var login = graphql(jsonHeaders(), body);
    final var token = Objects.requireNonNull(login.findValue("token"),
      "admin login response must contain a token").asString();
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  protected JsonNode graphql(final HttpHeaders headers, final String query) {
    final var response = rest.post()
      .uri(url())
      .contentType(MediaType.APPLICATION_JSON)
      .headers(httpHeaders -> httpHeaders.putAll(headers))
      .body(query)
      .retrieve()
      .body(String.class);
    final var body = Objects.requireNonNull(response, "GraphQL response body must not be null");
    return mapper.readTree(body);
  }

  protected String submitPostingAndGetId(final HttpHeaders headers) {
    final var body = """
      {"query":"mutation($i:SubmitJobInput!){submitJobPosting(input:$i){id}}",\
      "variables":{"i":{"url":"https://example.com/job","title":"Engineer","company":"Acme","description":"Software engineer role","source":"LINKEDIN"}}}
      """;
    final var submitted = graphql(headers, body);
    return Objects.requireNonNull(submitted.findValue("id"),
      "submit response must contain a posting id").asString();
  }

  protected JsonNode submitJobPosting(final HttpHeaders headers, final String url,
                                      final String title, final String company, final String source) {
    final var body = """
      {"query":"mutation($i:SubmitJobInput!){submitJobPosting(input:$i){id title company source}}",\
      "variables":{"i":{"url":"https://example.com/job/%s","title":"%s",\
      "description":"No empty","company":"%s","source":"%s"}}}
      """.formatted(url, title, company, source);
    final var submitted = graphql(headers, body);
    return Objects.requireNonNull(submitted, "submit response must not be null");
  }
}
