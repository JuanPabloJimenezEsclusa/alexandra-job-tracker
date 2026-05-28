package com.jobtracker.cli.command;

import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import com.jobtracker.cli.session.SessionManager;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

/**
 * Shell commands for authentication.
 */
@ShellComponent
public class AuthCommands {
  private final GraphqlClient client;
  private final SessionManager session;

  /**
   * Constructs auth commands with the given client and session manager.
   */
  public AuthCommands(final GraphqlClient client, final SessionManager session) {
    this.client = client;
    this.session = session;
  }

  /**
   * Registers a new user and saves the session token.
   */
  @ShellMethod(key = {"register", "reg"}, value = "Register a new user", group = "Authentication")
  public String register(
    @ShellOption(value = {"--username", "-u"}) final String username,
    @ShellOption(value = {"--password", "-p"}) final String password) {
    final var result = client.execute("""
        mutation($u: String!, $p: String!) {
          register(username: $u, password: $p) { token }
        }""",
      Map.of("u", username, "p", password));
    final var data = result.get("data");
    if (data == null || data.get("register") == null) {
      final var errors = result.get("errors");
      return errors != null ? "Error: " + errors.get(0).get("message").asText() : "Request failed";
    }
    session.saveToken(data.get("register").get("token").asText());
    return "Registered and logged in as %s".formatted(username);
  }

  /**
   * Logs in with username and password and saves the session token.
   */
  @ShellMethod(key = {"login", "li"}, value = "Login with username and password", group = "Authentication")
  public String login(
    @ShellOption(value = {"--username", "-u"}) final String username,
    @ShellOption(value = {"--password", "-p"}) final String password) {
    final var result = client.execute("""
        mutation($u: String!, $p: String!) {
          login(username: $u, password: $p) { token }
        }""",
      Map.of("u", username, "p", password));
    final var data = result.get("data");
    if (data == null || data.get("login") == null) {
      final var errors = result.get("errors");
      return errors != null ? "Error: " + errors.get(0).get("message").asText() : "Request failed";
    }
    session.saveToken(data.get("login").get("token").asText());
    return "Logged in as " + username;
  }

  /**
   * Logs out by clearing the session token.
   */
  @ShellMethod(key = {"logout", "lo"}, value = "Logout", group = "Authentication")
  public String logout() {
    session.clearToken();
    return "Logged out";
  }

  /**
   * Displays the currently logged-in user.
   */
  @ShellMethod(key = {"whoami", "who"}, value = "Show current user", group = "Authentication")
  public String whoami() {
    final var result = client.execute("{ me { username } }", Map.of());
    final var data = result.get("data");
    if (data == null || data.get("me") == null || data.get("me").isNull()) {
      return "Not logged in";
    }
    return "User: " + data.get("me").get("username").asText();
  }
}
