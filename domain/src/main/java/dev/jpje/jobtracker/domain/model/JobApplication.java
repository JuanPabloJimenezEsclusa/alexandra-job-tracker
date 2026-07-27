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

import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

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

  public JobApplication {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(source, "source must not be null");
    Objects.requireNonNull(status, "status must not be null");
    Objects.requireNonNull(dateApplied, "dateApplied must not be null");
    Objects.requireNonNull(lastUpdated, "lastUpdated must not be null");
    requireNonBlank(company, "company must not be blank");
    requireNonBlank(role, "role must not be blank");
  }

  private static void requireNonBlank(final String value, final String message) {
    if (value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
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
      throw new IllegalStateException("Cannot transition from " + status + " to " + newStatus);
    }
    return new JobApplication(id, userId, company, role, source, postingUrl, newStatus, dateApplied, lastUpdated, notes);
  }

  public JobApplication withNotes(@Nullable final String notes, final Instant lastUpdated) {
    return new JobApplication(id, userId, company, role, source, postingUrl, status, dateApplied, lastUpdated, notes);
  }
}
