package dev.jpje.jobtracker.domain.exception;

public class InvalidStateTransitionException extends DomainException {

  public InvalidStateTransitionException(final String message) {
    super(message);
  }
}
