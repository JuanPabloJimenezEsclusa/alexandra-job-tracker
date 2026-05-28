package dev.jpje.jobtracker.domain.port.outbound;

import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;

public interface SaveJobApplicationPort {
  JobApplication save(JobApplication application);

  void delete(UUID id);
}
