package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class AnalyticsIntegrationTest extends GraphQlIntegrationTestBase {

  @Test
  void shouldReturnAnalytics() {
    final var headers = jsonHeaders();
    headers.setBearerAuth(registerAndGetToken("analytics-user"));
    final var postingId = submitPostingAndGetId(headers);

    graphql(headers, """
      {"query": "mutation { createApplication(jobPostingId: \\"%s\\") { id } }"}
      """.formatted(postingId));

    final var analytics = graphql(headers, """
      {"query": "{ analytics { totalApplications perStatus { saved } } }"}
      """);
    final var total = analytics.findValue("totalApplications").asInt();
    final var saved = analytics.findValue("saved").asInt();
    assertThat(total).as("analytics total applications").isGreaterThanOrEqualTo(1);
    assertThat(saved).as("analytics saved count equals total").isEqualTo(total);
  }
}
