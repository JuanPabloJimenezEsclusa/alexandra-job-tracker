package com.jobtracker.domain.vo;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object wrapping a UUID user identifier.
 */
public record UserId(UUID value) {

  /**
   * Instantiates a new User id.
   */
  public UserId {
    Objects.requireNonNull(value, "value must not be null");
  }

  /**
   * Creates a new UserId with a random UUID.
   */
  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }
}
