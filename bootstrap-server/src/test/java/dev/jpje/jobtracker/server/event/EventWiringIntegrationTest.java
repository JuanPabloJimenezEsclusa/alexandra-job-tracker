package dev.jpje.jobtracker.server.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

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
  void shouldCreateTrackingApplicationOnSubmit() {
    final var headers = authHeaders("event-user");
    final var postingId = submitPostingAndGetId(headers);

    await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
      final var applications = graphql(headers, """
        {"query": "{ applications { jobPostingId status } }"}
        """);
      assertThat(applications.findValues("jobPostingId"))
        .as("tracking application created for submitted posting")
        .extracting(JsonNode::asString)
        .contains(postingId);
      assertThat(applications.findValues("status"))
        .as("tracking application status")
        .extracting(JsonNode::asString)
        .contains("SAVED");
    });
  }

  @Test
  void shouldPersistJobAnalysisOnSubmit() {
    final var headers = authHeaders("event-analysis-user");
    final var postingId = submitPostingAndGetId(headers);

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
