package com.jobtracker.api.resolver;

import org.jspecify.annotations.Nullable;

/**
 * Sanitizes string inputs from external sources.
 */
public final class StringSanitizer {
  private StringSanitizer() {
  }

  /**
   * Removes control characters (0x00–0x1F) except {@code \n}, {@code \r}, {@code \t}.
   */
  public static @Nullable String sanitize(@Nullable final String value) {
    if (value == null) { return null; }
    return value.chars()
      .filter(c -> c >= 0x20 || c == '\n' || c == '\r' || c == '\t')
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString()
      .trim();
  }
}
