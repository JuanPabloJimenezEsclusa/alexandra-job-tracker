package com.jobtracker.cli.session;

/**
 * Exception thrown when a session operation fails.
 */
public class SessionException extends RuntimeException {
  /**
   * Instantiates a new Session exception.
   */
  public SessionException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
