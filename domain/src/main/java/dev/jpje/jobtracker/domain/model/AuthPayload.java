package dev.jpje.jobtracker.domain.model;

import java.util.Objects;

public record AuthPayload(String token, User user) {

  public AuthPayload {
    Objects.requireNonNull(token, "token must not be null");
    Objects.requireNonNull(user, "user must not be null");
  }
}
