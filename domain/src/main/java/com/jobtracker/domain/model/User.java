package com.jobtracker.domain.model;

import java.time.Instant;

import com.jobtracker.domain.vo.UserId;

/**
 * A registered user of the job tracker system.
 */
public record User(
  UserId id,
  String username,
  String passwordHash,
  Instant createdAt) {
}
