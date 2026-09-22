package dev.jpje.jobtracker.api.error;

public class ForbiddenException extends RuntimeException {

  public static final String CODE = "FORBIDDEN";
  public static final String CLASSIFICATION = "DOMAIN";

  public ForbiddenException(final String message) {
    super(message);
  }
}
