package com.jobtracker.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AuthCommandsIntegrationTest extends BaseCliIntegrationTest {

  private static final Consumer<AuthCommandsIntegrationTest> NO_OP = _ -> {};
  private static final Consumer<AuthCommandsIntegrationTest> STUB_ERROR = _ -> stubGraphqlError();
  private static final Consumer<AuthCommandsIntegrationTest> STUB_ME_NULL = _ ->
    stubGraphql("me", """
      {"data": {"me": null}}
      """);
  private static final Consumer<AuthCommandsIntegrationTest> STUB_ME_USER = _ ->
    stubGraphql("me", """
      {"data": {"me": {"username": "preloaded"}}}
      """);
  private static final Consumer<AuthCommandsIntegrationTest> STUB_REGISTER = _ ->
    stubGraphql("register", """
      {"data": {"register": {"token": "reg-jwt-token"}}}
      """);
  private static final Consumer<AuthCommandsIntegrationTest> STUB_REGISTER_ERROR = _ ->
    stubGraphql("register", """
      {"errors": [{"message": "Username already taken"}]}
      """);
  private static final Consumer<AuthCommandsIntegrationTest> AUTH = t -> {
    try {
      t.authenticate();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  };

  private static Stream<Arguments> scenarios() {
    return Stream.of(
      arguments(named("login with valid credentials", NO_OP), NO_OP,
        "login --username preloaded --password pass", "Logged in as"),
      arguments(named("login with invalid credentials", STUB_ERROR), NO_OP,
        "login --username preloaded --password wrongpass", "Error"),
      arguments(named("whoami without authentication", STUB_ME_NULL), NO_OP,
        "whoami", "Not logged in"),
      arguments(named("logout", NO_OP), AUTH,
        "logout", "Logged out"),
      arguments(named("whoami after login", STUB_ME_USER), AUTH,
        "whoami", "preloaded"),
      arguments(named("register with valid credentials", STUB_REGISTER), NO_OP,
        "register --username newuser --password secret", "Registered and logged in as"),
      arguments(named("register with duplicate username", STUB_REGISTER_ERROR), NO_OP,
        "register --username existing --password pass", "Error")
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("scenarios")
  void shouldHandleCommand(final Consumer<AuthCommandsIntegrationTest> stub,
                           final Consumer<AuthCommandsIntegrationTest> auth,
                           final String command,
                           final String expected) throws Exception {
    stub.accept(this);
    auth.accept(this);
    final var result = shell.sendCommand(command);
    assertThat(result.lines()).anyMatch(line -> line.contains(expected));
  }
}
