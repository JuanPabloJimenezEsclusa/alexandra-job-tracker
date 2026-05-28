package dev.jpje.jobtracker.domain.port.in;

import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.vo.UserId;

public interface AnalyzeJobPostingPort {
  JobAnalysisRecord analyze(UserId userId, UUID jobPostingId);
}
