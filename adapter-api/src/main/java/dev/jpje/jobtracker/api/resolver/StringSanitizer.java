package dev.jpje.jobtracker.api.resolver;

public final class StringSanitizer {
  private StringSanitizer() {
  }

  public static String sanitize(final String value) {
    return value.chars()
      .filter(c -> c >= 0x20 || c == '\n' || c == '\r' || c == '\t')
      .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
      .toString()
      .trim();
  }
}
