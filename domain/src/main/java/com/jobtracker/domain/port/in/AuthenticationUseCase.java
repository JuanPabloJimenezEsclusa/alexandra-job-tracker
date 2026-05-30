package com.jobtracker.domain.port.in;

import java.util.Optional;

import com.jobtracker.domain.model.AuthPayload;
import com.jobtracker.domain.model.User;
import com.jobtracker.domain.vo.UserId;

/**
 * Use case for user registration and login.
 */
public interface AuthenticationUseCase {
  /**
   * Registers a new user with the given username and password.
   */
  AuthPayload register(String username, String password);

  /**
   * Authenticates a user by username and password.
   */
  AuthPayload login(String username, String password);

  /**
   * Returns the current user for the given user ID.
   */
  Optional<User> getCurrentUser(UserId userId);
}
