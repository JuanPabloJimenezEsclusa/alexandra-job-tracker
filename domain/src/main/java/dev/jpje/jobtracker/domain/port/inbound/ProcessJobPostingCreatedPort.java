package dev.jpje.jobtracker.domain.port.inbound;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;

public interface ProcessJobPostingCreatedPort {
  void createTracking(JobPostingCreated event);

  void analyzePosting(JobPostingCreated event);
}
