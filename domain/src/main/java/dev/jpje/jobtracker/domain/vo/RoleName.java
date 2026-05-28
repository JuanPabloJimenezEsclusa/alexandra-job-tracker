package dev.jpje.jobtracker.domain.vo;

import java.util.Objects;

public record RoleName(String value) {
  private static final int MAX_LENGTH = 150;

  public RoleName {
    Objects.requireNonNull(value, "role must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("role must not be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("role must not exceed " + MAX_LENGTH + " characters");
    }
  }

  public static RoleName of(final String value) {
    return new RoleName(value);
  }
}
