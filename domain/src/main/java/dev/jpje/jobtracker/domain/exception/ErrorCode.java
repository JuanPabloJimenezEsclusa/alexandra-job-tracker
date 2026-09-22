package dev.jpje.jobtracker.domain.exception;

public enum ErrorCode {
  NOT_FOUND("NOT_FOUND", "DOMAIN"),
  CONFLICT("CONFLICT", "DOMAIN"),
  INVALID_STATE("INVALID_STATE", "DOMAIN");

  private final String code;
  private final String classification;

  ErrorCode(final String code, final String classification) {
    this.code = code;
    this.classification = classification;
  }

  public String code() {
    return code;
  }

  public String classification() {
    return classification;
  }
}
