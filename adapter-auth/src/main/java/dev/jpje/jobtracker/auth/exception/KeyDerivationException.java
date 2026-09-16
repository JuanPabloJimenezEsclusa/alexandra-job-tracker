package dev.jpje.jobtracker.auth.exception;

public class KeyDerivationException extends RuntimeException {
  public KeyDerivationException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
