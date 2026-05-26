package com.jobtracker.domain.port.out;

import com.jobtracker.domain.model.User;

public interface SaveUserPort {
  void save(User user);
}
