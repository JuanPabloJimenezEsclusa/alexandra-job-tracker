package dev.jpje.jobtracker.domain.exception;

public class ForbiddenException extends DomainException {

  public ForbiddenException(final String message) {
    super(message);
  }
}
