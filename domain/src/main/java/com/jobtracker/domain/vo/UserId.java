package com.jobtracker.domain.vo;

import java.util.UUID;

/**
 * Value object wrapping a UUID user identifier.
 */
public record UserId(UUID value) {
  /**
   * Creates a new UserId with a random UUID.
   */
  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }
}
