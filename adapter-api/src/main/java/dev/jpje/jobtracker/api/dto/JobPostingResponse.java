package dev.jpje.jobtracker.api.dto;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.Source;
import org.jspecify.annotations.Nullable;

public record JobPostingResponse(
    UUID id,
    String url,
    Source source,
    String title,
    String company,
    @Nullable String description,
    @Nullable Instant postedAt) {

  public static JobPostingResponse from(final JobPosting posting) {
    Objects.requireNonNull(posting, "posting must not be null");
    return new JobPostingResponse(
      posting.id(),
      posting.url().value(),
      posting.source(),
      posting.title().value(),
      posting.company().value(),
      posting.description(),
      posting.postedAt());
  }
}
