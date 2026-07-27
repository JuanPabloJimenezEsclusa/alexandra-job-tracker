package dev.jpje.jobtracker.domain.service;

import java.time.Instant;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;

public class ApplicationTracker {
  public JobApplication transitionStatus(final JobApplication application,
                                         final ApplicationStatus newStatus,
                                         final Instant lastUpdated) {
    return application.withStatus(newStatus, lastUpdated);
  }
}
