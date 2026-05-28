package dev.jpje.jobtracker.domain.vo;

import java.util.Objects;

import dev.jpje.jobtracker.domain.model.User;

public record AuthPayload(String token, User user) {

  public AuthPayload {
    Objects.requireNonNull(token, "token must not be null");
    Objects.requireNonNull(user, "user must not be null");
  }
}
