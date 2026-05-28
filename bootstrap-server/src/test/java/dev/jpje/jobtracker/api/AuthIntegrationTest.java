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
class AuthIntegrationTest extends GraphQlIntegrationTestBase {

  @Test
  void shouldRegister() {
    final var registration = graphql(jsonHeaders(), """
      {"query": "mutation { register(username: \\"alice\\", password: \\"pass\\") { token user { username } } }"}
      """);
    assertThat(registration.findValue("token")).as("register returns token").isNotNull();
    assertThat(registration.findValues("username")).as("registered user username")
      .extracting(JsonNode::asString).contains("alice");
  }

  @Test
  void shouldRejectDuplicateRegistration() {
    final var body = """
      {"query": "mutation { register(username: \\"bob\\", password: \\"pass\\") { token } }"}
      """;
    graphql(jsonHeaders(), body);
    final var duplicate = graphql(jsonHeaders(), body);
    assertThat(duplicate.findValue("message").asString()).as("duplicate registration rejected")
      .isEqualTo("Username already taken");
  }

  @Test
  void shouldLogin() {
    graphql(jsonHeaders(), """
      {"query": "mutation { register(username: \\"carol\\", password: \\"pass\\") { token } }"}
      """);
    final var login = graphql(jsonHeaders(), """
      {"query": "mutation { login(username: \\"carol\\", password: \\"pass\\") { token user { username } } }"}
      """);
    assertThat(login.findValue("token")).as("login returns token").isNotNull();
    assertThat(login.findValues("username")).as("logged in user username")
      .extracting(JsonNode::asString).contains("carol");
  }

  @Test
  void shouldRejectInvalidLogin() {
    final var login = graphql(jsonHeaders(), """
      {"query": "mutation { login(username: \\"nobody\\", password: \\"wrong\\") { token } }"}
      """);
    assertThat(login.findValue("message").asString()).as("invalid login rejected")
      .isEqualTo("Invalid credentials");
  }

  @Test
  void shouldReturnMe() {
    final var headers = jsonHeaders();
    headers.setBearerAuth(registerAndGetToken("dave"));
    final var me = graphql(headers, """
      {"query": "{ me { username } }"}
      """);
    assertThat(me.findValues("username")).as("current user username")
      .extracting(JsonNode::asString).contains("dave");
  }

  @Test
  void shouldReturnNullMeWithoutAuth() {
    final var me = graphql(jsonHeaders(), """
      {"query": "{ me { username } }"}
      """);
    assertThat(me.findValue("username")).as("me without auth is null").isNull();
  }

  @Test
  void shouldLogout() {
    final var logout = graphql(jsonHeaders(), """
      {"query": "mutation { logout }"}
      """);
    assertThat(logout.findValue("logout").asBoolean()).as("logout result").isTrue();
  }
}
