package dev.jpje.jobtracker.domain.port.inbound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.vo.UserId;

public interface ManageJobAnalysisPort {
  Optional<JobAnalysisRecord> findByIdForUser(UserId userId, UUID id);

  List<JobAnalysisRecord> findByUserId(UserId userId);

  void delete(UUID id);
}
