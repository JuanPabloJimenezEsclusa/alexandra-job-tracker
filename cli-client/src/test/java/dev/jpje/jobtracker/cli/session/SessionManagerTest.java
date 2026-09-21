package dev.jpje.jobtracker.cli.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SessionManagerTest {

  @TempDir
  private Path tempDir;

  @Test
  void shouldPersistToken() {
    final var manager = new SessionManager(tempDir.toString());

    manager.saveToken("token-value");

    assertThat(readSessionFile()).contains("token-value");
  }

  @Test
  void shouldReturnEmptyWhenNoSession() {
    assertThat(new SessionManager(tempDir.toString()).loadToken()).isEmpty();
  }

  @Test
  void shouldLoadSavedToken() {
    writeSessionFile("""
    { "token": "token-value" }""");

    assertThat(new SessionManager(tempDir.toString()).loadToken()).isEqualTo("token-value");
  }

  @Test
  void shouldClearSession() {
    writeSessionFile("""
    { "token": "token-value" }""");

    new SessionManager(tempDir.toString()).clearToken();

    assertThat(Files.exists(sessionFile())).isFalse();
  }

  @Test
  void shouldThrowWhenSaveFails() {
    prepareParentBlocked();
    final var sessionManager = new SessionManager(tempDir.toString());

    assertThatThrownBy(() -> sessionManager.saveToken("t"))
      .isInstanceOf(SessionException.class);
  }

  @Test
  void shouldThrowWhenLoadFails() {
    writeInvalidSessionFile();
    final var sessionManager = new SessionManager(tempDir.toString());

    assertThatThrownBy(sessionManager::loadToken)
      .isInstanceOf(SessionException.class);
  }

  @Test
  void shouldThrowWhenClearFails() {
    prepareNonEmptySessionDirectory();
    final var sessionManager = new SessionManager(tempDir.toString());

    assertThatThrownBy(sessionManager::clearToken)
      .isInstanceOf(SessionException.class);
  }

  @Test
  void shouldExposeLoadFailureDetails() {
    final var cause = new IllegalStateException("boom");

    assertThat(new SessionException("Failed to load session", cause))
      .hasMessage("Failed to load session")
      .hasCause(cause);
  }

  private Path sessionFile() {
    return tempDir.resolve(".job-tracker").resolve("session");
  }

  private String readSessionFile() {
    try {
      return Files.readString(sessionFile());
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void writeSessionFile(final String content) {
    try {
      Files.createDirectories(sessionFile().getParent());
      Files.writeString(sessionFile(), content);
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void writeInvalidSessionFile() {
    writeSessionFile("{not valid json");
  }

  private void prepareParentBlocked() {
    try {
      Files.writeString(tempDir.resolve(".job-tracker"), "blocked");
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void prepareNonEmptySessionDirectory() {
    try {
      Files.createDirectories(sessionFile().resolve("inner"));
    } catch (final IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
