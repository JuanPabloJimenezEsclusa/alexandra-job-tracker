package com.jobtracker.domain.port.in;

import com.jobtracker.domain.model.User;

/**
 * Use case for user registration and login.
 */
public interface AuthenticationUseCase {
  /**
   * Registers a new user with the given username and password.
   */
  User register(String username, String password);

  /**
   * Authenticates a user by username and password.
   */
  User login(String username, String password);
}
