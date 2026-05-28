package dev.jpje.jobtracker.domain.vo;

import org.jspecify.annotations.Nullable;

public record Notes(@Nullable String value) {
  public Notes {
    if (value != null && value.isBlank()) {
      throw new IllegalArgumentException("notes must not be blank");
    }
  }

  public static Notes of(final @Nullable String value) {
    return new Notes(value);
  }

  public static Notes empty() {
    return new Notes(null);
  }

  public boolean isEmpty() {
    return value == null;
  }
}
