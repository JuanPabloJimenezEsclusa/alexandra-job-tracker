package dev.jpje.jobtracker.api.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.User;

public record UserResponse(UUID id, String username, Instant createdAt) {

  public static UserResponse from(final User user) {
    Objects.requireNonNull(user, "user must not be null");
    return new UserResponse(user.id().value(), user.username().value(), user.createdAt());
  }
}
