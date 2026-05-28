package com.jobtracker.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.vo.UserId;

/**
 * Port for loading job posting data from persistence.
 */
public interface LoadJobPostingPort {
  /**
   * Finds a job posting by its ID.
   */
  Optional<JobPosting> findById(UUID id);

  /**
   * Finds all job postings for the given user.
   */
  List<JobPosting> findByUserId(UserId userId);
}
