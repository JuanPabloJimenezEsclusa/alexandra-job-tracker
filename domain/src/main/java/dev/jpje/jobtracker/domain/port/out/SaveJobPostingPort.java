package dev.jpje.jobtracker.domain.port.out;

import dev.jpje.jobtracker.domain.model.JobPosting;

public interface SaveJobPostingPort {
  void save(JobPosting posting);
}
