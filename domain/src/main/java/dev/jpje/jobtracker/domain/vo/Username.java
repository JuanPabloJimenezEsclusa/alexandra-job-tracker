package dev.jpje.jobtracker.domain.vo;

import java.util.Objects;
import java.util.regex.Pattern;

public record Username(String value) {
  private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");

  public Username {
    Objects.requireNonNull(value, "username must not be null");
    if (!PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
        "username must be 3-50 alphanumeric characters (._- allowed)");
    }
  }

  public static Username of(final String value) {
    return new Username(value);
  }
}
