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

    graphql(headers, """
      {"query": "mutation { createApplication(company: \\"X\\", role: \\"Dev\\", source: LINKEDIN) { id } }"}
      """);

    final var analytics = graphql(headers, """
      {"query": "{ analytics { totalApplications perStatus { saved } } }"}
      """);
    assertThat(analytics.findValue("totalApplications")).as("analytics total applications").isNotNull();
    assertThat(analytics.findValue("saved")).as("analytics saved count").isNotNull();
  }
}
