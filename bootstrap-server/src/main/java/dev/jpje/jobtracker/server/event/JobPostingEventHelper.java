package dev.jpje.jobtracker.server.event;

import java.time.Instant;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.outbound.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import io.micrometer.core.instrument.Counter;

final class JobPostingEventHelper {

  private JobPostingEventHelper() {
  }

  static void createTracking(final SaveJobApplicationPort saveAppPort,
                             final Counter applicationCreatedCounter,
                             final JobPostingCreated event,
                             final Instant now) {
    final var posting = event.jobPosting();
    final var tracking = new JobApplication(
      UUID.randomUUID(),
      posting.userId(),
      posting.id(),
      ApplicationStatus.SAVED,
      now,
      now,
      null,
      null);
    saveAppPort.save(tracking);
    applicationCreatedCounter.increment();
  }

  static void analyzePosting(final JobAnalysisPort analysisPort,
                             final SaveJobAnalysisPort saveAnalysisPort,
                             final JobPostingCreated event,
                             final Instant now) {
    final var posting = event.jobPosting();
    final var analysis = analysisPort.analyze(
      posting.title().value(), posting.company().value(), posting.source().name(),
      posting.description());
    final var jobAnalysisRecord = new JobAnalysisRecord(
      UUID.randomUUID(),
      posting.id(),
      posting.userId(),
      analysis,
      now);
    saveAnalysisPort.saveOrReplace(jobAnalysisRecord);
  }
}
