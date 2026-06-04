package com.jobtracker.api.resolver;

/**
 * Sanitizes string inputs from external sources.
 */
public final class StringSanitizer {
  private StringSanitizer() {
  }

  /**
   * Removes control characters (0x00–0x1F) except {@code \n}, {@code \r}, {@code \t}.
   */
  public static String sanitize(final String value) {
    return value.chars()
      .filter(c -> c >= 0x20 || c == '\n' || c == '\r' || c == '\t')
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString()
      .trim();
  }
}
