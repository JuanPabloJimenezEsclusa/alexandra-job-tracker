package dev.jpje.jobtracker.domain.port.in;

import java.util.Optional;

import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.vo.AuthPayload;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;

public interface AuthenticationPort {
  AuthPayload register(Username username, String password, UserRole role);
  AuthPayload login(Username username, String password);
  Optional<User> getCurrentUser(UserId userId);
}
