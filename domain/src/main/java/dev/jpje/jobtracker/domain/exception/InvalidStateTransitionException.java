package dev.jpje.jobtracker.domain.exception;

public class InvalidStateTransitionException extends DomainException {

  public InvalidStateTransitionException(final String message) {
    super(message);
  }

  @Override
  public ErrorCode errorCode() {
    return ErrorCode.INVALID_STATE;
  }
}
