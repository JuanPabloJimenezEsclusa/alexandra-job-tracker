package dev.jpje.jobtracker.application.usecase;

import java.time.Clock;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.inbound.ProcessJobPostingCreatedPort;
import dev.jpje.jobtracker.domain.port.outbound.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;

public class ProcessJobPostingCreatedUseCase implements ProcessJobPostingCreatedPort {
  private final SaveJobApplicationPort saveApplicationPort;
  private final JobAnalysisPort analysisPort;
  private final SaveJobAnalysisPort saveAnalysisPort;
  private final Clock clock;

  public ProcessJobPostingCreatedUseCase(final SaveJobApplicationPort saveApplicationPort,
                                         final JobAnalysisPort analysisPort,
                                         final SaveJobAnalysisPort saveAnalysisPort,
                                         final Clock clock) {
    this.saveApplicationPort = saveApplicationPort;
    this.analysisPort = analysisPort;
    this.saveAnalysisPort = saveAnalysisPort;
    this.clock = clock;
  }

  @Override
  public void createTracking(final JobPostingCreated event) {
    final var posting = event.jobPosting();
    final var now = clock.instant();
    final var tracking = new JobApplication(
      UUID.randomUUID(),
      posting.userId(),
      posting.id(),
      ApplicationStatus.SAVED,
      now,
      now,
      null,
      null);
    saveApplicationPort.save(tracking);
  }

  @Override
  public void analyzePosting(final JobPostingCreated event) {
    final var posting = event.jobPosting();
    final var analysis = analysisPort.analyze(
      posting.title().value(), posting.company().value(), posting.source().name(),
      posting.description());
    final var jobAnalysisRecord = new JobAnalysisRecord(
      UUID.randomUUID(),
      posting.id(),
      posting.userId(),
      analysis,
      clock.instant());
    saveAnalysisPort.saveOrReplace(jobAnalysisRecord);
  }
}
