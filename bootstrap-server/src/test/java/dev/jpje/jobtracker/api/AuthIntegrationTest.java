package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.server.JobTrackerServerApplication;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.JsonNode;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class AuthIntegrationTest extends GraphQlIntegrationTestBase {

  private static final String TEST_SECRET = "super-secret-signing-key-for-tests";
  private static final String AUTHENTICATION_REQUIRED = "Authentication required";

  @Test
  void shouldRegister() {
    final var registration = graphql(adminHeaders(), """
      {"query": "mutation { register(username: \\"alice\\", password: \\"pass\\", role: USER) { token user { username role } } }"}
      """);
    assertThat(registration.findValue("token")).as("register returns token").isNotNull();
    assertThat(registration.findValues("username")).as("registered user username")
      .extracting(JsonNode::asString).contains("alice");
    assertThat(registration.findValues("role")).as("registered user role")
      .extracting(JsonNode::asString).contains("USER");
  }

  @Test
  void shouldRejectDuplicateRegistration() {
    final var body = """
      {"query": "mutation { register(username: \\"bob\\", password: \\"pass\\", role: USER) { token } }"}
      """;
    graphql(adminHeaders(), body);
    final var duplicate = graphql(adminHeaders(), body);
    assertThat(duplicate.findValue("message").asString()).as("duplicate registration rejected")
      .isEqualTo("Username already taken");
  }

  @Test
  void shouldRejectRegisterWithoutAuth() {
    final var body = """
      {"query": "mutation { register(username: \\"eve\\", password: \\"pass\\", role: USER) { token } }"}
      """;
    final var registration = graphql(jsonHeaders(), body);
    assertThat(registration.findValue("message").asString()).as("register without auth rejected")
      .isEqualTo(AUTHENTICATION_REQUIRED);
  }

  @Test
  void shouldRejectRegisterForNonAdmin() {
    final var userHeaders = jsonHeaders();
    userHeaders.setBearerAuth(registerAndGetToken("mallory"));
    final var registration = graphql(userHeaders, """
      {"query": "mutation { register(username: \\"trent\\", password: \\"pass\\", role: USER) { token } }"}
      """);
    assertThat(registration.findValue("message").asString()).as("register as non-admin rejected")
      .isEqualTo("Admin access required");
  }

  @Test
  void shouldRegisterAdmin() {
    final var registration = graphql(adminHeaders(), """
      {"query": "mutation { register(username: \\"admin2\\", password: \\"pass\\", role: ADMIN) { token user { role } } }"}
      """);
    assertThat(registration.findValues("role")).as("admin role on registered user")
      .extracting(JsonNode::asString).contains("ADMIN");
  }

  @Test
  void shouldLogin() {
    graphql(adminHeaders(), """
      {"query": "mutation { register(username: \\"carol\\", password: \\"pass\\", role: USER) { token } }"}
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
    assertThat(login.findValue("errorCode").asString()).as("invalid login error code")
      .isEqualTo("BAD_REQUEST");
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

  private static Stream<Arguments> invalidTokenScenarios() {
    return Stream.of(
      arguments(named("expired token", expiredToken())),
      arguments(named("malformed token", "not-a-jwt"))
    );
  }

  @ParameterizedTest(name = "{0} treated as unauthenticated")
  @MethodSource("invalidTokenScenarios")
  void shouldTreatInvalidTokenAsUnauthenticated(final String token) {
    // Given
    final var headers = jsonHeaders();
    headers.setBearerAuth(token);

    // When
    final var apps = graphql(headers, """
      {"query": "{ applications { id } }"}
      """);

    // Then
    assertThat(apps.findValue("message").asString()).as("an invalid token is treated as unauthenticated")
      .isEqualTo(AUTHENTICATION_REQUIRED);
  }

  private static String expiredToken() {
    try {
      final var key = Keys.hmacShaKeyFor(
        MessageDigest.getInstance("SHA-512").digest(TEST_SECRET.getBytes(StandardCharsets.UTF_8)));
      return Jwts.builder()
        .subject(UUID.randomUUID().toString())
        .claim("role", "USER")
        .expiration(Date.from(Instant.EPOCH))
        .signWith(key)
        .compact();
    } catch (final NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
