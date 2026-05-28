package dev.jpje.jobtracker.domain.port.out;

import dev.jpje.jobtracker.domain.vo.JobAnalysis;

public interface JobAnalysisPort {
  JobAnalysis analyze(String jobDescription);
}
