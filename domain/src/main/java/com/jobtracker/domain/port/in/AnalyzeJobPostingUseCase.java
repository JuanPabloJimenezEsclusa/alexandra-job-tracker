package com.jobtracker.domain.port.in;

import java.util.UUID;

import com.jobtracker.domain.model.JobAnalysis;

/**
 * Use case for analyzing a job posting's content.
 */
public interface AnalyzeJobPostingUseCase {
  /**
   * Analyzes the job posting with the given ID and returns the analysis result.
   */
  JobAnalysis analyze(UUID jobPostingId);
}
