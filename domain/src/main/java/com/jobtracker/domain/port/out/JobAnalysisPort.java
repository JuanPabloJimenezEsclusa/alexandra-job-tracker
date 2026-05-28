package com.jobtracker.domain.port.out;

import com.jobtracker.domain.model.JobAnalysis;

/**
 * Port for analyzing job descriptions using AI.
 */
public interface JobAnalysisPort {
  /**
   * Analyzes the given job description and returns the analysis result.
   */
  JobAnalysis analyze(String jobDescription);
}
