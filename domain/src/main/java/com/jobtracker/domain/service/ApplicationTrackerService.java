package com.jobtracker.domain.service;

import java.time.Instant;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;

/**
 * The type Application tracker service.
 */
public class ApplicationTrackerService {
  /**
   * Transition status job application.
   */
  public JobApplication transitionStatus(final JobApplication application,
                                         final ApplicationStatus newStatus,
                                         final Instant lastUpdated) {
    return application.withStatus(newStatus, lastUpdated);
  }
}
