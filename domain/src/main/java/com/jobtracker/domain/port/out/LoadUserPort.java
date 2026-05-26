package com.jobtracker.domain.port.out;

import java.util.Optional;

import com.jobtracker.domain.model.User;
import com.jobtracker.domain.vo.UserId;

public interface LoadUserPort {
  Optional<User> findByUsername(String username);
  Optional<User> findById(UserId id);
}
