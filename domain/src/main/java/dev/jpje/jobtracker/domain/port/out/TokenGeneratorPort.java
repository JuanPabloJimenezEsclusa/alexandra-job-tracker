package dev.jpje.jobtracker.domain.port.out;

import dev.jpje.jobtracker.domain.vo.UserId;

public interface TokenGeneratorPort {
  String generateToken(UserId userId);
}
