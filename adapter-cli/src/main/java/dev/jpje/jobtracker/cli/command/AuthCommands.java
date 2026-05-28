package dev.jpje.jobtracker.cli.command;

import java.util.Map;

import dev.jpje.jobtracker.cli.client.GraphqlClient;
import dev.jpje.jobtracker.cli.session.SessionManager;
import org.jspecify.annotations.Nullable;
import org.springframework.shell.core.command.annotation.Command;
import org.springframework.shell.core.command.annotation.Option;
import org.springframework.stereotype.Component;

@Component
public class AuthCommands {
  private final GraphqlClient client;
  private final SessionManager session;

  public AuthCommands(final GraphqlClient client, final SessionManager session) {
    this.client = client;
    this.session = session;
  }

  private static String resolvePassword(@Nullable final String provided) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    final var console = System.console();
    if (console == null) {
      throw new IllegalArgumentException("Password required. Use --password/-p in non-interactive mode.");
    }
    final var chars = console.readPassword("Password: ");
    return new String(chars);
  }

  @Command(
    name = "register",
    alias = {"reg"},
    description = "Register a new user.",
    group = "Authentication",
    help = """
      Registers a new user with the provided username and password.
      On success, logs in the user and saves the session token for future authenticated requests.
      
      Example usage:
        - register -u alice
        - reg -u bob -p secret123
      """)
  public String register(
    @Option(
      longName = "username", shortName = 'u',
      description = "Username", required = true) final String username,
    @Option(
      longName = "password", shortName = 'p',
      description = "Password (omit to be prompted securely)") final String password) {
    final var resolved = resolvePassword(password);
    final var result = client.execute("""
        mutation($u: String!, $p: String!) {
          register(username: $u, password: $p) { token }
        }""",
      Map.of("u", username, "p", resolved));
    final var data = result.get("data");
    if (data == null || data.isNull() || data.get("register") == null) {
      return result.toPrettyString();
    }
    session.saveToken(data.get("register").get("token").asText());
    return "Registered and logged in as %s".formatted(username);
  }

  @Command(
    name = "login",
    alias = {"li"},
    description = "Login with username and password.",
    group = "Authentication",
    help = """
      Logs in with the provided username and password.
      On success, saves the session token for future authenticated requests.
      
      Example usage:
        - login -u alice
        - li -u bob -p secret123
      """)
  public String login(
    @Option(
      longName = "username", shortName = 'u',
      description = "Username", required = true) final String username,
    @Option(
      longName = "password", shortName = 'p',
      description = "Password (omit to be prompted securely)") final String password) {
    final var resolved = resolvePassword(password);
    final var result = client.execute("""
        mutation($u: String!, $p: String!) {
          login(username: $u, password: $p) { token }
        }""",
      Map.of("u", username, "p", resolved));
    final var data = result.get("data");
    if (data == null || data.isNull() || data.get("login") == null) {
      return result.toPrettyString();
    }
    session.saveToken(data.get("login").get("token").asText());
    return "Logged in as " + username;
  }

  @Command(
    name = "logout",
    alias = {"lo"},
    description = "Logout.",
    group = "Authentication",
    help = """
      Logs out the current user by clearing the session token.
      
      Example usage:
        - logout
        - lo
      """)
  public String logout() {
    session.clearToken();
    return "Logged out";
  }

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
        - who
      """)
  public String whoami() {
    final var result = client.execute("{ me { username } }", Map.of());
    final var data = result.get("data");
    if (data == null || data.isNull() || data.get("me") == null || data.get("me").isNull()) {
      return "Not logged in";
    }
    return "User: " + data.get("me").get("username").asText();
  }
}
