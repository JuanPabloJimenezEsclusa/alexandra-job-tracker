package com.jobtracker.domain.port.out;

import com.jobtracker.domain.model.JobPosting;

/**
 * Port for persisting job posting data.
 */
public interface SaveJobPostingPort {
  /**
   * Saves a job posting.
   */
  void save(JobPosting posting);
}
