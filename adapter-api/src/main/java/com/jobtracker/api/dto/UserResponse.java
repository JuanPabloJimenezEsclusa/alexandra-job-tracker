package com.jobtracker.api.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.jobtracker.domain.model.User;

/**
 * API response for a user, excluding sensitive fields like passwordHash.
 */
public record UserResponse(UUID id, String username, Instant createdAt) {

  /**
   * Maps a domain User to an API response DTO.
   */
  public static UserResponse from(final User user) {
    Objects.requireNonNull(user, "user must not be null");
    return new UserResponse(user.id().value(), user.username(), user.createdAt());
  }
}
