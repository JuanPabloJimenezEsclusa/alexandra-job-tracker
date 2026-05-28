package dev.jpje.jobtracker.domain.vo;

import java.util.Objects;

public record JobTitle(String value) {
  private static final int MAX_LENGTH = 200;

  public JobTitle {
    Objects.requireNonNull(value, "title must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("title must not be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("title must not exceed " + MAX_LENGTH + " characters");
    }
  }

  public static JobTitle of(final String value) {
    return new JobTitle(value);
  }
}
