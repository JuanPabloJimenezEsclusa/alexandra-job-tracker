package com.jobtracker.domain.port.out;

import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;

/**
 * Port for persisting job application data.
 */
public interface SaveJobApplicationPort {
  /**
   * Saves a job application.
   */
  void save(JobApplication application);

  /**
   * Deletes a job application by ID.
   */
  void delete(UUID id);
}
