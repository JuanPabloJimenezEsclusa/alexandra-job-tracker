package com.jobtracker.domain.model;

import static com.jobtracker.domain.vo.ApplicationStatus.ACCEPTED;
import static com.jobtracker.domain.vo.ApplicationStatus.APPLIED;
import static com.jobtracker.domain.vo.ApplicationStatus.INTERVIEWING;
import static com.jobtracker.domain.vo.ApplicationStatus.OFFER;
import static com.jobtracker.domain.vo.ApplicationStatus.REJECTED;
import static com.jobtracker.domain.vo.ApplicationStatus.WITHDRAWN;

import java.time.Instant;
import java.util.UUID;

import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

/**
 * A job application tracked by a user.
 */
public record JobApplication(
  UUID id,
  UserId userId,
  String company,
  String role,
  Source source,
  @Nullable String postingUrl,
  ApplicationStatus status,
  Instant dateApplied,
  Instant lastUpdated,
  @Nullable String notes) {

  private static boolean canTransitionTo(final ApplicationStatus current, final ApplicationStatus target) {
    return switch (current) {
      case SAVED -> target == APPLIED || target == WITHDRAWN;
      case APPLIED -> target == INTERVIEWING || target == REJECTED || target == WITHDRAWN;
      case INTERVIEWING -> target == OFFER || target == REJECTED || target == WITHDRAWN;
      case OFFER -> target == ACCEPTED || target == REJECTED || target == WITHDRAWN;
      case ACCEPTED, REJECTED, WITHDRAWN -> false;
    };
  }

  /**
   * Returns a new JobApplication with the given status after validating the transition.
   */
  public JobApplication withStatus(final ApplicationStatus newStatus) {
    if (!canTransitionTo(status, newStatus)) {
      throw new IllegalStateException("Cannot transition from " + status + " to " + newStatus);
    }
    return new JobApplication(id, userId, company, role, source, postingUrl, newStatus, dateApplied, Instant.now(), notes);
  }

  /**
   * Returns a new JobApplication with updated notes.
   */
  public JobApplication withNotes(@Nullable final String notes) {
    return new JobApplication(id, userId, company, role, source, postingUrl, status, dateApplied, Instant.now(), notes);
  }
}
