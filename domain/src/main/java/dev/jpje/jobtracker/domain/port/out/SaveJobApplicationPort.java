package dev.jpje.jobtracker.domain.port.out;

import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;

public interface SaveJobApplicationPort {
  void save(JobApplication application);

  void delete(UUID id);
}
