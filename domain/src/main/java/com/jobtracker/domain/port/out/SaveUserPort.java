package com.jobtracker.domain.port.out;

import com.jobtracker.domain.model.User;

/**
 * Port for persisting user data.
 */
public interface SaveUserPort {
  /**
   * Saves a user.
   */
  void save(User user);
}
