package com.jobtracker.domain.model;

import java.time.Instant;
import java.util.Objects;
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

  /**
   * Instantiates a new Job posting.
   */
  public JobPosting {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(source, "source must not be null");
    Objects.requireNonNull(postedAt, "postedAt must not be null");
    requireNonBlank(url, "url must not be blank");
    requireNonBlank(title, "title must not be blank");
    requireNonBlank(description, "description must not be blank");
    requireNonBlank(company, "company must not be blank");
  }

  private static void requireNonBlank(final String value, final String message) {
    if (value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
  }
}
