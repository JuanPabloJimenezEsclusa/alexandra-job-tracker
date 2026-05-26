package com.jobtracker.cli.session;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {
  private static final Path SESSION_FILE = Path.of(System.getProperty("user.home"), ".job-tracker", "session");
  private final ObjectMapper mapper = new ObjectMapper();

  public void saveToken(final String token) {
    try {
      Files.createDirectories(SESSION_FILE.getParent());
      mapper.writeValue(SESSION_FILE.toFile(), Map.of("token", token));
    } catch (Exception e) {
      throw new RuntimeException("Failed to save session", e);
    }
  }

  public String loadToken() {
    try {
      if (Files.exists(SESSION_FILE))
        return mapper.readTree(SESSION_FILE.toFile()).get("token").asText();
    } catch (Exception _) {
    }
    return "";
  }

  public void clearToken() {
    try {
      Files.deleteIfExists(SESSION_FILE);
    } catch (Exception _) {
    }
  }
}
