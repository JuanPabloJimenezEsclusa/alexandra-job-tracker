package com.jobtracker.domain.port.in;

import java.util.List;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;

/**
 * Use case for tracking job applications.
 */
public interface TrackJobApplicationUseCase {
  /**
   * Creates a new job application.
   */
  JobApplication create(UserId userId, String company, String role, Source source, String postingUrl, String notes);

  /**
   * Updates the status of an existing application.
   */
  JobApplication updateStatus(UUID applicationId, ApplicationStatus newStatus, String notes);

  /**
   * Lists job applications for a user, optionally filtered.
   */
  List<JobApplication> list(UserId userId, ApplicationStatus status, Source source);

  /**
   * Deletes a job application.
   */
  void delete(UUID applicationId);
}
