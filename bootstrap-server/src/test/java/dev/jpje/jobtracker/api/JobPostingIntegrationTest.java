package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;

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
class JobPostingIntegrationTest extends GraphQlIntegrationTestBase {

  @Test
  void shouldListJobPostings() {
    final var headers = authHeaders("jp-list-user");
    final var postings = graphql(headers, """
      {"query": "{ jobPostings { id title company } }"}
      """);
    assertThat(postings.findValues("title")).as("no postings for new user").isEmpty();
  }

  @Test
  void shouldSubmitJobPosting() {
    final var headers = authHeaders("submit-user");
    final var submitted = submitJobPosting(headers, "test-engineer", "Test Engineer", "TestCorp", "LINKEDIN");
    assertThat(submitted.findValues("title")).as("submitted posting title")
      .extracting(JsonNode::asString).contains("Test Engineer");
    assertThat(submitted.findValues("company")).as("submitted posting company")
      .extracting(JsonNode::asString).contains("TestCorp");
    assertThat(submitted.findValues("source")).as("submitted posting source")
      .extracting(JsonNode::asString).contains("LINKEDIN");
  }

  @Test
  void shouldListSubmittedPosting() {
    final var headers = authHeaders("submit-list-user");
    submitJobPosting(headers, "listed-job", "Listed Engineer", "ListedCorp", "LINKEDIN");

    final var postings = graphql(headers, """
      {"query": "{ jobPostings { title company } }"}
      """);
    assertThat(postings.findValues("title")).as("submitted posting listed")
      .extracting(JsonNode::asString).contains("Listed Engineer");
  }

  @Test
  void shouldFilterPostingsBySource() {
    final var headers = authHeaders("jp-filter-user");
    submitJobPosting(headers, "linkedin-job", "LinkedIn Job", "LinkedCorp", "LINKEDIN");

    final var postings = graphql(headers, """
      {"query": "{ jobPostings(source: INDEED) { title } }"}
      """);
    assertThat(postings.findValues("title")).as("postings filtered by source").isEmpty();
  }
}
