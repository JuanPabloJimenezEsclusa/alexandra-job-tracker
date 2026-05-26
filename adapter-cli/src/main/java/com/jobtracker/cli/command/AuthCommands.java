package com.jobtracker.cli.command;

import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import com.jobtracker.cli.session.SessionManager;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class AuthCommands {
  private final GraphqlClient client;
  private final SessionManager session;

  public AuthCommands(final GraphqlClient client, final SessionManager session) {
    this.client = client;
    this.session = session;
  }

  @ShellMethod(value = "Register a new user", group = "Authentication")
  public String register(final String username, final String password) {
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

  @ShellMethod(value = "Login with username and password", group = "Authentication")
  public String login(final String username, final String password) {
    final var result = client.execute( """
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

  @ShellMethod(value = "Logout", group = "Authentication")
  public String logout() {
    session.clearToken();
    return "Logged out";
  }

  @ShellMethod(value = "Show current user", group = "Authentication")
  public String whoami() {
    final var result = client.execute("{ me { username } }", Map.of());
    final var data = result.get("data");
    if (data == null || data.get("me") == null) {
      return "Not logged in";
    }
    return "User: " + data.get("me").get("username").asText();
  }
}
