package dev.jpje.jobtracker.domain.vo;

import java.util.Objects;

public record CompanyName(String value) {
  private static final int MAX_LENGTH = 200;

  public CompanyName {
    Objects.requireNonNull(value, "company name must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("company name must not be blank");
    }
    if (value.length() > MAX_LENGTH) {
      throw new IllegalArgumentException("company name must not exceed " + MAX_LENGTH + " characters");
    }
  }

  public static CompanyName of(final String value) {
    return new CompanyName(value);
  }
}
