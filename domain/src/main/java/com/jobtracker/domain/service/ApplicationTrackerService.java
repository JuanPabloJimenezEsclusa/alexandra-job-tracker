package com.jobtracker.domain.service;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;

/**
 * Service for managing job application status transitions.
 */
public class ApplicationTrackerService {
  /**
   * Transitions an application to the new status, throwing if the transition is invalid.
   */
  public JobApplication transitionStatus(final JobApplication application, final ApplicationStatus newStatus) {
    return application.withStatus(newStatus);
  }
}
