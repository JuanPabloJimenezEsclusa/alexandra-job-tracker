package dev.jpje.jobtracker.domain.vo;

import java.net.URI;
import java.util.Objects;

public record Url(String value) {
  public Url {
    Objects.requireNonNull(value, "url must not be null");
    if (value.isBlank()) {
      throw new IllegalArgumentException("url must not be blank");
    }
    try {
      new URI(value).toURL();
    } catch (final Exception e) {
      throw new IllegalArgumentException("Invalid URL: " + value, e);
    }
  }

  public static Url of(final String value) {
    return new Url(value);
  }
}
