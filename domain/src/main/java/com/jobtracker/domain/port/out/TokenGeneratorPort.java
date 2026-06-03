package com.jobtracker.domain.port.out;

import com.jobtracker.domain.vo.UserId;

/**
 * Port for generating authentication tokens.
 */
public interface TokenGeneratorPort {
  /**
   * Generates an authentication token for the given user.
   */
  String generateToken(UserId userId);
}
