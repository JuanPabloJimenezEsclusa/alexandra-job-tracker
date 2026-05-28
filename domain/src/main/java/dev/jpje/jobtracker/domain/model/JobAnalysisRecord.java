package dev.jpje.jobtracker.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.UserId;

public record JobAnalysisRecord(
  UUID id,
  UUID jobPostingId,
  UserId userId,
  JobAnalysis analysis,
  Instant createdAt) {

  public JobAnalysisRecord {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(jobPostingId, "jobPostingId must not be null");
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(analysis, "analysis must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
  }
}
