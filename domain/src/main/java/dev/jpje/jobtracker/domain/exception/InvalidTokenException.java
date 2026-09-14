package dev.jpje.jobtracker.domain.exception;

public class InvalidTokenException extends DomainException {

  public InvalidTokenException(final String message) {
    super(message);
  }

  public InvalidTokenException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
