package dev.jpje.jobtracker.domain.port.outbound;

import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;

public interface SaveJobAnalysisPort {
  void saveOrReplace(JobAnalysisRecord jobAnalysisRecord);

  void delete(UUID id);
}
