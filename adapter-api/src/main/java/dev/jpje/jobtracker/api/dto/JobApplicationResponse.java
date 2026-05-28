package dev.jpje.jobtracker.api.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import org.jspecify.annotations.Nullable;

public record JobApplicationResponse(
    UUID id,
    UUID jobPostingId,
    ApplicationStatus status,
    Instant dateApplied,
    Instant lastUpdated,
    @Nullable String notes) {

  public static JobApplicationResponse from(final JobApplication app) {
    Objects.requireNonNull(app, "app must not be null");
    final var notes = app.notes();
    return new JobApplicationResponse(
      app.id(),
      app.jobPostingId(),
      app.status(),
      app.dateApplied(),
      app.lastUpdated(),
      notes != null ? notes.value() : null);
  }
}
