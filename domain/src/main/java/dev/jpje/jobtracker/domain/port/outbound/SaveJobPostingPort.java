package dev.jpje.jobtracker.domain.port.outbound;

import dev.jpje.jobtracker.domain.model.JobPosting;

public interface SaveJobPostingPort {
  void save(JobPosting posting);
}
