package dev.jpje.jobtracker.api.dto;

import java.util.Objects;

import dev.jpje.jobtracker.domain.vo.AuthPayload;

public record AuthPayloadResponse(String token, UserResponse user) {

  public static AuthPayloadResponse from(final AuthPayload payload) {
    Objects.requireNonNull(payload, "payload must not be null");
    return new AuthPayloadResponse(payload.token(), UserResponse.from(payload.user()));
  }
}
