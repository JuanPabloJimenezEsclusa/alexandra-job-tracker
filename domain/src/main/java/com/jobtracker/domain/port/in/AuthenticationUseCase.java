package com.jobtracker.domain.port.in;

import com.jobtracker.domain.model.User;

public interface AuthenticationUseCase {
  User register(String username, String password);

  User login(String username, String password);
}
