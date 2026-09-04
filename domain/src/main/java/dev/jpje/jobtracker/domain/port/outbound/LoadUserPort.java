package dev.jpje.jobtracker.domain.port.outbound;

import java.util.Optional;

import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.vo.UserId;

public interface LoadUserPort {
  Optional<User> findByUsername(String username);

  Optional<User> findById(UserId id);
}
