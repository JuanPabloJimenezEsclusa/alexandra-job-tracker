package dev.jpje.jobtracker.domain.vo;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {

  public UserId {
    Objects.requireNonNull(value, "value must not be null");
  }

  public static UserId generate() {
    return new UserId(UUID.randomUUID());
  }
}
