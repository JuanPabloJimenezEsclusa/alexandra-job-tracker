package dev.jpje.jobtracker.domain.model;

import java.time.Instant;
import java.util.Objects;

import dev.jpje.jobtracker.domain.vo.UserId;

public record User(
  UserId id,
  String username,
  String passwordHash,
  Instant createdAt) {

  public User {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    requireNonBlank(username, "username must not be blank");
    requireNonBlank(passwordHash, "passwordHash must not be blank");
  }

  private static void requireNonBlank(final String value, final String message) {
    if (value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }
}
