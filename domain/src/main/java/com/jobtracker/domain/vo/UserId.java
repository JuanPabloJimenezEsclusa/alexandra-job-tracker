package com.jobtracker.domain.vo;

import java.util.UUID;

public record UserId(UUID value) {
  public UserId {
    if (value == null) throw new IllegalArgumentException("UserId must not be null");
  }

  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }
}
