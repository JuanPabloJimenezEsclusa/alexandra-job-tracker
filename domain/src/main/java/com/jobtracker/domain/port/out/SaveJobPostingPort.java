package com.jobtracker.domain.port.out;

import com.jobtracker.domain.model.JobPosting;

public interface SaveJobPostingPort {
  void save(JobPosting posting);
}
