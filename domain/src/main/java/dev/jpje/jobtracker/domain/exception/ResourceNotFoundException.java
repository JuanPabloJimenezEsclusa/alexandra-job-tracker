package dev.jpje.jobtracker.domain.exception;

public class ResourceNotFoundException extends DomainException {

  public ResourceNotFoundException(final String message) {
    super(message);
  }
}
