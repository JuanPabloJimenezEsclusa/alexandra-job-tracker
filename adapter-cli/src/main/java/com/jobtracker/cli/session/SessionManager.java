package com.jobtracker.cli.session;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Manages persistent user session data.
 */
@Component
public class SessionManager {
  private static final Path SESSION_FILE = Path.of(System.getProperty("user.home"), ".job-tracker", "session");
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Saves the authentication token to the session file.
   */
  public void saveToken(final String token) {
    try {
      Files.createDirectories(SESSION_FILE.getParent());
      mapper.writeValue(SESSION_FILE.toFile(), Map.of("token", token));
    } catch (final Exception e) {
      throw new SessionException("Failed to save session", e);
    }
  }

  /**
   * Loads the authentication token from the session file.
   */
  public String loadToken() {
    try {
      if (Files.exists(SESSION_FILE)) {
        return mapper.readTree(SESSION_FILE.toFile()).get("token").asText();
      }
    } catch (final Exception e) {
      throw new SessionException("Failed to load session", e);
    }
    return "";
  }

  /**
   * Clears the saved session token.
   */
  public void clearToken() {
    try {
      Files.deleteIfExists(SESSION_FILE);
    } catch (final Exception e) {
      throw new SessionException("Failed to clear session", e);
    }
  }
}
