package dev.jpje.jobtracker.domain.port.out;

import dev.jpje.jobtracker.domain.vo.TokenPayload;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;

public interface TokenGeneratorPort {
  String generateToken(UserId userId, UserRole role);

  TokenPayload validateToken(String token);
}
