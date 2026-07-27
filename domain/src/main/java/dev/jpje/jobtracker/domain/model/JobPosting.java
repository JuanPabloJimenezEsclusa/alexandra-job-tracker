package dev.jpje.jobtracker.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;

public record JobPosting(
  UUID id,
  UserId userId,
  Url url,
  Source source,
  JobTitle title,
  CompanyName company,
  String description,
  Instant postedAt) {

  public JobPosting {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(url, "url must not be null");
    Objects.requireNonNull(source, "source must not be null");
    Objects.requireNonNull(title, "title must not be null");
    Objects.requireNonNull(company, "company must not be null");
    Objects.requireNonNull(postedAt, "postedAt must not be null");
    if (description.isBlank()) {
      throw new IllegalArgumentException("description must not be blank");
    }
  }
}
