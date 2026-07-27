package dev.jpje.jobtracker.domain.model;

import java.time.Instant;
import java.util.Objects;

import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.Username;

public record User(
  UserId id,
  Username username,
  String passwordHash,
  Instant createdAt) {

  public User {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(username, "username must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    if (passwordHash.isBlank()) {
      throw new IllegalArgumentException("passwordHash must not be blank");
    }
  }
}
