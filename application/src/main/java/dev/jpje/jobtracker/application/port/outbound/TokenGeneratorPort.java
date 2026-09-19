package dev.jpje.jobtracker.application.port.outbound;

import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;

public interface TokenGeneratorPort {
  String generateToken(UserId userId, UserRole role);
}
