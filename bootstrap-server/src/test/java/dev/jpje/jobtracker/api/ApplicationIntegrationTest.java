package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import tools.jackson.databind.JsonNode;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class ApplicationIntegrationTest extends GraphQlIntegrationTestBase {

  @Test
  void shouldCreateApplication() {
    final var headers = authHeaders("create-app-user");
    final var created = graphql(headers, createBody("alpha", "SWE", "LINKEDIN"));
    assertThat(created.findValue("status").asString()).as("created application status").isEqualTo("SAVED");
  }

  @Test
  void shouldListApplications() {
    final var headers = authHeaders("list-app-user");
    graphql(headers, createBody("beta", "SWE", "LINKEDIN"));

    final var applications = graphql(headers, """
      {"query": "{ applications { company role status } }"}
      """);
    assertThat(applications.findValues("company")).as("created application listed")
      .extracting(JsonNode::asString).contains("beta");
  }

  @Test
  void shouldUpdateApplicationStatus() {
    final var headers = authHeaders("update-app-user");
    final var created = graphql(headers, createBody("gamma", "PM", "INDEED"));
    final var appId = Objects.requireNonNull(created.findValue("id"),
      "create response must contain an application id").asString();

    final var updated = graphql(headers, """
      {"query": "mutation { updateApplicationStatus(id: \\"%s\\", status: APPLIED) { status } }"}
      """.formatted(appId));
    assertThat(updated.findValue("status").asString()).as("updated application status").isEqualTo("APPLIED");
    assertThat(updated.has("errors")).as("update mutation succeeded without errors").isFalse();
  }

  @Test
  void shouldDeleteApplication() {
    final var headers = authHeaders("delete-app-user");
    final var created = graphql(headers, createBody("delta", "PM", "INDEED"));
    final var appId = Objects.requireNonNull(created.findValue("id"),
      "create response must contain an application id").asString();

    final var deleted = graphql(headers, """
      {"query": "mutation { deleteApplication(id: \\"%s\\") }"}
      """.formatted(appId));
    assertThat(deleted.findValue("deleteApplication").asBoolean()).as("delete mutation result").isTrue();
  }

  @Test
  void shouldFilterApplicationsByStatus() {
    final var headers = authHeaders("filter-user");
    graphql(headers, createBody("epsilon", "Dev", "OTHER"));

    final var applications = graphql(headers, """
      {"query": "{ applications(status: APPLIED) { company } }"}
      """);
    assertThat(applications.findValues("company")).as("applications filtered by status")
      .extracting(JsonNode::asString).isEmpty();
  }

  private HttpHeaders authHeaders(final String username) {
    final var headers = jsonHeaders();
    headers.setBearerAuth(registerAndGetToken(username));
    return headers;
  }

  private static String createBody(final String company, final String role, final String source) {
    return """
      {"query": "mutation { createApplication(company: \\"%s\\", role: \\"%s\\", postingUrl: \\"https://job\\", source: %s) { id status } }"}
      """.formatted(company, role, source);
  }
}
