package com.jobtracker.api.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import org.jspecify.annotations.Nullable;

/**
 * API response for a job application.
 */
public record JobApplicationResponse(
    UUID id,
    String company,
    String role,
    Source source,
    @Nullable String postingUrl,
    ApplicationStatus status,
    Instant dateApplied,
    Instant lastUpdated,
    @Nullable String notes) {

  /**
   * Maps a domain JobApplication to an API response DTO.
   */
  public static JobApplicationResponse from(final JobApplication app) {
    Objects.requireNonNull(app, "app must not be null");
    return new JobApplicationResponse(
      app.id(),
      app.company(),
      app.role(),
      app.source(),
      app.postingUrl(),
      app.status(),
      app.dateApplied(),
      app.lastUpdated(),
      app.notes());
  }
}
