package dev.jpje.jobtracker.domain.exception;

public class OptimisticLockException extends DomainException {

  public OptimisticLockException(final String message, final Throwable cause) {
    super(message, cause);
  }

  @Override
  public ErrorCode errorCode() {
    return ErrorCode.CONFLICT;
  }
}
