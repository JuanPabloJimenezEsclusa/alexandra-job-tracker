package com.jobtracker.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

/**
 * Port for loading job application data from persistence.
 */
public interface LoadJobApplicationPort {
  /**
   * Finds a job application by its ID.
   */
  Optional<JobApplication> findById(UUID id);

  /**
   * Finds job applications by user ID, optionally filtered by status and source.
   */
  List<JobApplication> findByUserId(UserId userId, @Nullable ApplicationStatus status, @Nullable Source source);

  /**
   * Finds all job applications for the given user.
   */
  List<JobApplication> findAllByUserId(UserId userId);
}
