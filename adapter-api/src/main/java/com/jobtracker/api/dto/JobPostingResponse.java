package com.jobtracker.api.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.vo.Source;
import org.jspecify.annotations.Nullable;

/**
 * API response for a job posting.
 */
public record JobPostingResponse(
    UUID id,
    String url,
    Source source,
    String title,
    String company,
    @Nullable String description,
    @Nullable Instant postedAt) {

  /**
   * Maps a domain JobPosting to an API response DTO.
   */
  public static JobPostingResponse from(final JobPosting posting) {
    Objects.requireNonNull(posting, "posting must not be null");
    return new JobPostingResponse(
      posting.id(),
      posting.url(),
      posting.source(),
      posting.title(),
      posting.company(),
      posting.description(),
      posting.postedAt());
  }
}
