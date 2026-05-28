package dev.jpje.jobtracker.domain.port.out;

import dev.jpje.jobtracker.domain.model.User;

public interface SaveUserPort {
  void save(User user);
}
