package dev.jpje.jobtracker.domain.port.outbound;

import dev.jpje.jobtracker.domain.vo.JobAnalysis;

public interface JobAnalysisPort {
  JobAnalysis analyze(String title, String company, String source, String jobDescription);
}
