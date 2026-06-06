package com.jobtracker.api.dto;

import java.util.Objects;

import com.jobtracker.domain.model.AuthPayload;

/**
 * API response for authentication operations.
 */
public record AuthPayloadResponse(String token, UserResponse user) {

  /**
   * Maps a domain AuthPayload to an API response DTO.
   */
  public static AuthPayloadResponse from(final AuthPayload payload) {
    Objects.requireNonNull(payload, "payload must not be null");
    return new AuthPayloadResponse(payload.token(), UserResponse.from(payload.user()));
  }
}
