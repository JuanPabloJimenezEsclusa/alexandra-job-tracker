package dev.jpje.jobtracker.domain.exception;

import org.jspecify.annotations.Nullable;

public abstract class DomainException extends RuntimeException {

  protected DomainException(final String message) {
    super(message);
  }

  protected DomainException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public @Nullable ErrorCode errorCode() {
    return null;
  }
}
