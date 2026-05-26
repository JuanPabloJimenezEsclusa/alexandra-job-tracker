package com.jobtracker.domain.model;

import java.time.Instant;
import com.jobtracker.domain.vo.UserId;

public record User(
  UserId id,
  String username,
  String passwordHash,
  Instant createdAt) {}
