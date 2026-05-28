package dev.jpje.jobtracker.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.UserId;

public interface LoadJobPostingPort {
  Optional<JobPosting> findById(UUID id);

  List<JobPosting> findByUserId(UserId userId);
}
