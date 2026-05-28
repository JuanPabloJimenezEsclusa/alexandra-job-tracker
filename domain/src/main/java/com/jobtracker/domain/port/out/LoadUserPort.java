package com.jobtracker.domain.port.out;

import java.util.Optional;

import com.jobtracker.domain.model.User;
import com.jobtracker.domain.vo.UserId;

/**
 * Port for loading user data from persistence.
 */
public interface LoadUserPort {
  /**
   * Finds a user by username.
   */
  Optional<User> findByUsername(String username);

  /**
   * Finds a user by ID.
   */
  Optional<User> findById(UserId id);
}
