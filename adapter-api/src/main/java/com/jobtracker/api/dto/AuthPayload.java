package com.jobtracker.api.dto;

import com.jobtracker.domain.model.User;

/**
 * Authentication payload containing JWT token and user details.
 */
public record AuthPayload(String token, User user) {
}
