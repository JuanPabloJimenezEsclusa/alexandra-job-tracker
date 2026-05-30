package com.jobtracker.domain.model;

/**
 * Authentication payload containing JWT token and user details.
 */
public record AuthPayload(String token, User user) {
}
