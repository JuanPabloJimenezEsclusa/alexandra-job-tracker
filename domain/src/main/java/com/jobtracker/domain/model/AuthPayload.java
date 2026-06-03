package com.jobtracker.domain.model;

import java.util.Objects;

/**
 * Authentication payload containing JWT token and user details.
 */
public record AuthPayload(String token, User user) {

  /**
   * Instantiates a new Auth payload.
   */
  public AuthPayload {
    Objects.requireNonNull(token, "token must not be null");
    Objects.requireNonNull(user, "user must not be null");
  }
}
