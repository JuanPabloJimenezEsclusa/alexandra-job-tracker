package dev.jpje.jobtracker.domain.exception;

public class ResourceAlreadyExistsException extends DomainException {

  public ResourceAlreadyExistsException(final String message) {
    super(message);
  }

  public ResourceAlreadyExistsException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
