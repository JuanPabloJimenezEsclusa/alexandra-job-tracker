package dev.jpje.jobtracker.server.event;

import java.time.Clock;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import io.micrometer.core.instrument.Counter;
import org.springframework.stereotype.Component;

@Component
public class JobPostingEventProcessor {
  private final SaveJobApplicationPort saveAppPort;
  private final JobAnalysisPort analysisPort;
  private final SaveJobAnalysisPort saveAnalysisPort;
  private final Clock clock;
  private final Counter applicationCreatedCounter;

  public JobPostingEventProcessor(final SaveJobApplicationPort saveAppPort,
                                  final JobAnalysisPort analysisPort,
                                  final SaveJobAnalysisPort saveAnalysisPort,
                                  final Clock clock,
                                  final Counter applicationCreatedCounter) {
    this.saveAppPort = saveAppPort;
    this.analysisPort = analysisPort;
    this.saveAnalysisPort = saveAnalysisPort;
    this.clock = clock;
    this.applicationCreatedCounter = applicationCreatedCounter;
  }

  public void process(final JobPostingCreated event) {
    JobPostingEventHelper.createTracking(saveAppPort, applicationCreatedCounter, event, clock.instant());
    JobPostingEventHelper.analyzePosting(analysisPort, saveAnalysisPort, event, clock.instant());
  }
}
