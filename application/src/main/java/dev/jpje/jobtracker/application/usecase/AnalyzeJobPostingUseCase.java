package dev.jpje.jobtracker.application.usecase;

import java.util.UUID;

import dev.jpje.jobtracker.domain.port.in.AnalyzeJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;

public class AnalyzeJobPostingUseCase implements AnalyzeJobPostingPort {
  private final LoadJobPostingPort loadJobPostingPort;
  private final JobAnalysisPort analysisPort;

  public AnalyzeJobPostingUseCase(final LoadJobPostingPort loadJobPostingPort, final JobAnalysisPort analysisPort) {
    this.loadJobPostingPort = loadJobPostingPort;
    this.analysisPort = analysisPort;
  }

  @Override
  public JobAnalysis analyze(final UUID jobPostingId) {
    final var posting = loadJobPostingPort.findById(jobPostingId)
      .orElseThrow(() -> new IllegalArgumentException("Job posting not found"));
    return analysisPort.analyze(posting.description());
  }
}
