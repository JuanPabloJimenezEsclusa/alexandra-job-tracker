package dev.jpje.jobtracker.domain.vo;

import java.util.Objects;

public record TokenPayload(UserId userId, UserRole role) {

  public TokenPayload {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(role, "role must not be null");
  }
}
