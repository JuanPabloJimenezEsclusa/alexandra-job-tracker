package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class ApplicationIntegrationTest extends GraphQlIntegrationTestBase {

  @Test
  void shouldCreateApplication() {
    final var headers = authHeaders("create-app-user");
    final var postingId = submitPostingAndGetId(headers);
    final var created = graphql(headers, createBody(postingId));
    assertThat(created.findValue("status").asString()).as("created application status").isEqualTo("SAVED");
    assertThat(created.findValue("jobPostingId").asString()).as("created application posting")
      .isEqualTo(postingId);
  }

  @Test
  void shouldRejectCreateApplicationForMissingPosting() {
    final var headers = authHeaders("missing-posting-user");
    final var created = graphql(headers, createBody("00000000-0000-0000-0000-000000000000"));
    assertThat(created.findValue("message").asString()).as("create application for missing posting rejected")
      .isEqualTo("Job posting not found");
  }

  @Test
  void shouldListApplications() {
    final var headers = authHeaders("list-app-user");
    final var postingId = submitPostingAndGetId(headers);
    final var created = graphql(headers, createBody(postingId));
    final var appId = Objects.requireNonNull(created.findValue("id"),
      "create response must contain an application id").asString();

    final var applications = graphql(headers, """
      {"query": "{ applications { id jobPostingId status } }"}
      """);
    assertThat(applications.findValues("id")).as("created application listed")
      .extracting(JsonNode::asString).contains(appId);
    assertThat(applications.findValues("jobPostingId")).as("applications reference the posting")
      .extracting(JsonNode::asString).contains(postingId);
  }

  @Test
  void shouldUpdateApplicationStatus() {
    final var headers = authHeaders("update-app-user");
    final var postingId = submitPostingAndGetId(headers);
    final var created = graphql(headers, createBody(postingId));
    final var appId = Objects.requireNonNull(created.findValue("id"),
      "create response must contain an application id").asString();

    final var updated = graphql(headers, """
      {"query": "mutation { updateApplicationStatus(id: \\"%s\\", status: APPLIED) { status } }"}
      """.formatted(appId));
    assertThat(updated.findValue("status").asString()).as("updated application status").isEqualTo("APPLIED");
  }

  @Test
  void shouldDeleteApplication() {
    final var headers = authHeaders("delete-app-user");
    final var postingId = submitPostingAndGetId(headers);
    final var created = graphql(headers, createBody(postingId));
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
    final var postingId = submitPostingAndGetId(headers);
    graphql(headers, createBody(postingId));

    final var applications = graphql(headers, """
      {"query": "{ applications(status: APPLIED) { id } }"}
      """);
    assertThat(applications.findValues("id")).as("applications filtered by status").isEmpty();
  }

  private static String createBody(final String postingId) {
    return """
      {"query": "mutation { createApplication(jobPostingId: \\"%s\\") { id status jobPostingId } }"}
      """.formatted(postingId);
  }
}
