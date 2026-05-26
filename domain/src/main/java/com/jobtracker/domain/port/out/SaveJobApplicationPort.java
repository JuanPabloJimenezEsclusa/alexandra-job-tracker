package com.jobtracker.domain.port.out;

import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;

public interface SaveJobApplicationPort {
  void save(JobApplication application);
  void delete(UUID id);
}
