package dev.jpje.jobtracker.domain.exception;

public enum ErrorCode {
  NOT_FOUND("NOT_FOUND"),
  CONFLICT("CONFLICT"),
  INVALID_STATE("INVALID_STATE");

  private final String code;

  ErrorCode(final String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }
}
