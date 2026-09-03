package dev.jpje.jobtracker.application.usecase;

import java.time.Clock;
import java.util.UUID;

import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.port.in.AnalyzeJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.UserId;

public class AnalyzeJobPostingUseCase implements AnalyzeJobPostingPort {
  private final LoadJobPostingPort loadJobPostingPort;
  private final JobAnalysisPort analysisPort;
  private final SaveJobAnalysisPort saveAnalysisPort;
  private final Clock clock;

  public AnalyzeJobPostingUseCase(final LoadJobPostingPort loadJobPostingPort,
                                  final JobAnalysisPort analysisPort,
                                  final SaveJobAnalysisPort saveAnalysisPort,
                                  final Clock clock) {
    this.loadJobPostingPort = loadJobPostingPort;
    this.analysisPort = analysisPort;
    this.saveAnalysisPort = saveAnalysisPort;
    this.clock = clock;
  }

  @Override
  public JobAnalysisRecord analyze(final UserId userId, final UUID jobPostingId) {
    final var posting = loadJobPostingPort.findByIdAndUser(jobPostingId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("Job posting not found"));
    final var analysis = analysisPort.analyze(
      posting.title().value(), posting.company().value(), posting.source().name(),
      posting.description());
    final var jobAnalysisRecord = new JobAnalysisRecord(UUID.randomUUID(), jobPostingId, userId, analysis, clock.instant());
    saveAnalysisPort.saveOrReplace(jobAnalysisRecord);
    return jobAnalysisRecord;
  }
}
