package com.jobtracker.cli.command;

import java.util.Map;

import com.jobtracker.cli.client.GraphqlClient;
import com.jobtracker.cli.session.SessionManager;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

/**
 * Shell commands for authentication.
 */
@Component
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
  @Command(
    name = "register",
    alias = {"reg"},
    description = "Register a new user.",
    group = "Authentication",
    help = """
      Registers a new user with the provided username and password.
      On success, logs in the user and saves the session token for future authenticated requests.
      
      Example usage:
        - register -u alice -p secret123
        - reg -u bob -p pass456""")
  public String register(
    @Option(
      longName = "username", shortName = 'u',
      description = "Username", required = true) final String username,
    @Option(
      longName = "password", shortName = 'p',
      description = "Password", required = true) final String password) {
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
  @Command(
    name = "login",
    alias = {"li"},
    description = "Login with username and password.",
    group = "Authentication",
    help = """
      Logs in with the provided username and password.
      On success, saves the session token for future authenticated requests.
      
      Example usage:
        - login -u alice -p secret123
        - li -u bob -p pass456""")
  public String login(
    @Option(
      longName = "username", shortName = 'u',
      description = "Username", required = true) final String username,
    @Option(
      longName = "password", shortName = 'p',
      description = "Password", required = true) final String password) {
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
  @Command(
    name = "logout",
    alias = {"lo"},
    description = "Logout.",
    group = "Authentication",
    help = """
      Logs out the current user by clearing the session token.
      
      Example usage:
        - logout
        - lo""")
  public String logout() {
    session.clearToken();
    return "Logged out";
  }

  /**
   * Displays the currently logged-in user.
   */
  @Command(
    name = "whoami",
    alias = {"who"},
    description = "Show current user.",
    group = "Authentication",
    help = """ 
      Shows the username of the currently logged-in user.
      If no user is logged in, indicates that as well.
      
      Example usage:
        - whoami
        - who""")
  public String whoami() {
    final var result = client.execute("{ me { username } }", Map.of());
    final var data = result.get("data");
    if (data == null || data.get("me") == null || data.get("me").isNull()) {
      return "Not logged in";
    }
    return "User: " + data.get("me").get("username").asText();
  }
}
