package com.jobtracker.domain.port.in;

import java.util.UUID;

import com.jobtracker.domain.model.JobAnalysis;

public interface AnalyzeJobPostingUseCase {
  JobAnalysis analyze(UUID jobPostingId);
}
