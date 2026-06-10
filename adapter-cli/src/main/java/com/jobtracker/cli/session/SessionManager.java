package com.jobtracker.cli.session;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Manages persistent user session data.
 */
@Component
public class SessionManager {
  private final Path sessionFile;
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Instantiates a new Session manager.
   */
  public SessionManager(@Value("${user.home}") final String userHome) {
    this.sessionFile = Path.of(userHome, ".job-tracker", "session");
  }

  /**
   * Saves the authentication token to the session file.
   */
  public void saveToken(final String token) {
    try {
      Files.createDirectories(sessionFile.getParent());
      mapper.writeValue(sessionFile.toFile(), Map.of("token", token));
    } catch (final Exception e) {
      throw new SessionException("Failed to save session", e);
    }
  }

  /**
   * Loads the authentication token from the session file.
   */
  public String loadToken() {
    try {
      if (Files.exists(sessionFile)) {
        return mapper.readTree(sessionFile.toFile()).get("token").asText();
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
      Files.deleteIfExists(sessionFile);
    } catch (final Exception e) {
      throw new SessionException("Failed to clear session", e);
    }
  }
}
