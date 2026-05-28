package dev.jpje.jobtracker.server.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Objects;

import dev.jpje.jobtracker.api.GraphQlIntegrationTestBase;
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
class EventWiringIntegrationTest extends GraphQlIntegrationTestBase {

  @Test
  void shouldDispatchJobPostingCreatedToListeners() {
    final var headers = jsonHeaders();
    headers.setBearerAuth(registerAndGetToken("event-user"));

    final var submitBody = """
      {"query":"mutation($i:SubmitJobInput!){submitJobPosting(input:$i){id title company source}}",\
      "variables":{"i":{"url":"https://example.com/job/event","title":"Event Engineer",\
      "description":"No empty","company":"EventCorp","source":"LINKEDIN"}}}
      """;
    final var submitted = graphql(headers, submitBody);
    assertThat(submitted.findValues("title"))
      .as("submitted posting title")
      .extracting(JsonNode::asString)
      .contains("Event Engineer");
    assertThat(submitted.findValues("company"))
      .as("submitted posting company")
      .extracting(JsonNode::asString)
      .contains("EventCorp");
    final var postingId = Objects.requireNonNull(submitted.findValue("id"),
      "submit response must contain a posting id").asString();

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      final var applications = graphql(headers, """
        {"query": "{ applications { company status } }"}
        """);
      assertThat(applications.findValues("company"))
        .as("tracking application created for submitted posting")
        .extracting(JsonNode::asString)
        .contains("EventCorp");
      assertThat(applications.findValues("status"))
        .as("tracking application status")
        .extracting(JsonNode::asString)
        .contains("SAVED");
    });

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      final var analyses = graphql(headers, """
        {"query": "{ analyses { jobPostingId } }"}
        """);
      assertThat(analyses.findValues("jobPostingId"))
        .as("job analysis persisted for submitted posting")
        .extracting(JsonNode::asString)
        .contains(postingId);
    });
  }
}
