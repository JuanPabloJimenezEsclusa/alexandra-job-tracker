package com.jobtracker.domain.model;

import java.time.Instant;
import java.util.UUID;

import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;

/**
 * A job posting scraped from an external source.
 */
public record JobPosting(
  UUID id,
  UserId userId,
  String url,
  Source source,
  String title,
  String company,
  String description,
  Instant postedAt) {
}
