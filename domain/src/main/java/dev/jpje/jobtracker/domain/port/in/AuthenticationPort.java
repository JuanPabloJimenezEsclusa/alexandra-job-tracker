package dev.jpje.jobtracker.domain.port.in;

import java.util.Optional;

import dev.jpje.jobtracker.domain.model.AuthPayload;
import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.vo.UserId;

public interface AuthenticationPort {
  AuthPayload register(String username, String password);

  AuthPayload login(String username, String password);

  Optional<User> getCurrentUser(UserId userId);
}
