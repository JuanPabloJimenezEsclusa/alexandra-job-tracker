package com.jobtracker.domain.port.out;

import com.jobtracker.domain.model.JobAnalysis;

public interface JobAnalysisPort {
  JobAnalysis analyze(String jobDescription);
}
