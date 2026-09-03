package dev.jpje.jobtracker.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.vo.UserId;

public interface LoadJobAnalysisPort {
  Optional<JobAnalysisRecord> findByIdAndUser(UUID id, UserId userId);

  Optional<JobAnalysisRecord> findByJobPostingId(UUID jobPostingId);

  List<JobAnalysisRecord> findByUserId(UserId userId);
}
