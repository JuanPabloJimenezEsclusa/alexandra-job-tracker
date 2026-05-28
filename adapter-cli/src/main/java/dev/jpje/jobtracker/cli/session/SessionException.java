package dev.jpje.jobtracker.cli.session;

public class SessionException extends RuntimeException {
  public SessionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
