package com.jobtracker.application.service;

import java.util.UUID;

import com.jobtracker.domain.model.JobAnalysis;
import com.jobtracker.domain.port.in.AnalyzeJobPostingUseCase;
import com.jobtracker.domain.port.out.JobAnalysisPort;
import com.jobtracker.domain.port.out.LoadJobPostingPort;

public class AnalyzeJobPostingUseCaseImpl implements AnalyzeJobPostingUseCase {
  private final LoadJobPostingPort loadJobPostingPort;
  private final JobAnalysisPort analysisPort;

  public AnalyzeJobPostingUseCaseImpl(final LoadJobPostingPort loadJobPostingPort, final JobAnalysisPort analysisPort) {
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
