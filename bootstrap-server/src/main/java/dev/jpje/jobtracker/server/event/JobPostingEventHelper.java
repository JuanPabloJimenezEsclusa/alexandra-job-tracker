package dev.jpje.jobtracker.server.event;

import java.time.Instant;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.RoleName;
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
      posting.company(),
      RoleName.of(posting.title().value()),
      posting.source(),
      posting.url(),
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
