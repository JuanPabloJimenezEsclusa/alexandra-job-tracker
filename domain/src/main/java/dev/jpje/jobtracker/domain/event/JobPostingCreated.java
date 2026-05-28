package dev.jpje.jobtracker.domain.event;

import java.time.Instant;
import java.util.Objects;

import dev.jpje.jobtracker.domain.model.JobPosting;

public record JobPostingCreated(
  JobPosting jobPosting,
  Instant occurredAt) implements DomainEvent {

  public JobPostingCreated {
    Objects.requireNonNull(jobPosting, "jobPosting not be null");
    Objects.requireNonNull(occurredAt, "occurredAt must not be null");
  }

  public static JobPostingCreated of(final JobPosting jobPosting, final Instant occurredAt) {
    return new JobPostingCreated(jobPosting, occurredAt);
  }
}
