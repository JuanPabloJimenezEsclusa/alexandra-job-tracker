package com.jobtracker.auth;

/**
 * Exception thrown when cryptographic key derivation fails.
 */
public class KeyDerivationException extends RuntimeException {
  /**
   * Instantiates a new Key derivation exception.
   */
  public KeyDerivationException(final String message, final Throwable cause) {
    super(message, cause);
  }
}
