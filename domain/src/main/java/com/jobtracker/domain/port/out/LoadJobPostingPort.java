package com.jobtracker.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import com.jobtracker.domain.model.JobPosting;

public interface LoadJobPostingPort {
  Optional<JobPosting> findById(UUID id);
}
