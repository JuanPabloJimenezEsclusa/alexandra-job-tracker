package com.jobtracker.domain.service;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;

public class ApplicationTrackerService {
  public JobApplication transitionStatus(final JobApplication application, final ApplicationStatus newStatus) {
    return application.withStatus(newStatus);
  }
}
