package com.jobtracker.domain.vo;

import java.util.UUID;

/**
 * Value object wrapping a UUID user identifier.
 */
public record UserId(UUID value) {
  /**
   * Validates that the UUID value is non-null.
   */
  public UserId {
    if (value == null) {
      throw new IllegalArgumentException("UserId must not be null");
    }
  }

  /**
   * Creates a new UserId with a random UUID.
   */
  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }
}
