package dev.jpje.jobtracker.domain.model;

import static dev.jpje.jobtracker.domain.vo.ApplicationStatus.ACCEPTED;
import static dev.jpje.jobtracker.domain.vo.ApplicationStatus.APPLIED;
import static dev.jpje.jobtracker.domain.vo.ApplicationStatus.INTERVIEWING;
import static dev.jpje.jobtracker.domain.vo.ApplicationStatus.OFFER;
import static dev.jpje.jobtracker.domain.vo.ApplicationStatus.REJECTED;
import static dev.jpje.jobtracker.domain.vo.ApplicationStatus.WITHDRAWN;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.domain.exception.InvalidStateTransitionException;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public record JobApplication(
  UUID id,
  UserId userId,
  UUID jobPostingId,
  ApplicationStatus status,
  Instant dateApplied,
  Instant lastUpdated,
  @Nullable Notes notes,
  @Nullable Long version) {

  public JobApplication {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(jobPostingId, "jobPostingId must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(dateApplied, "dateApplied must not be null");
    Objects.requireNonNull(lastUpdated, "lastUpdated must not be null");
  }

  private static boolean canTransitionTo(final ApplicationStatus current, final ApplicationStatus target) {
    return switch (current) {
      case SAVED -> target == APPLIED || target == WITHDRAWN;
      case APPLIED -> target == INTERVIEWING || target == REJECTED || target == WITHDRAWN;
      case INTERVIEWING -> target == OFFER || target == REJECTED || target == WITHDRAWN;
      case OFFER -> target == ACCEPTED || target == REJECTED || target == WITHDRAWN;
      case ACCEPTED, REJECTED, WITHDRAWN -> false;
    };
  }

  public JobApplication withStatus(final ApplicationStatus newStatus, final Instant lastUpdated) {
    if (!canTransitionTo(status, newStatus)) {
      throw new InvalidStateTransitionException("Cannot transition from " + status + " to " + newStatus);
    }
    return new JobApplication(id, userId, jobPostingId, newStatus,
      dateApplied, lastUpdated, notes, version);
  }

  public JobApplication withNotes(@Nullable final Notes notes, final Instant lastUpdated) {
    return new JobApplication(id, userId, jobPostingId, status,
      dateApplied, lastUpdated, notes, version);
  }
}
