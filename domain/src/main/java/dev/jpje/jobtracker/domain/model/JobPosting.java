package dev.jpje.jobtracker.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;

public record JobPosting(
  UUID id,
  UserId userId,
  String url,
  Source source,
  String title,
  String company,
  String description,
  Instant postedAt) {

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
