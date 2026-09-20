package dev.jpje.jobtracker.application.port.outbound;

import dev.jpje.jobtracker.domain.model.User;

public interface AuthenticateUserPort {
  User authenticate(String username, String rawPassword);
}
