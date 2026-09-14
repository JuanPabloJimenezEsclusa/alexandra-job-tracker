package dev.jpje.jobtracker.events;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.port.inbound.ProcessJobPostingCreatedPort;
import org.springframework.stereotype.Component;

@Component
public class AnalysisJobPostingEventHandler implements JobPostingEventHandler {
  private final ProcessJobPostingCreatedPort processJobPostingCreatedPort;

  public AnalysisJobPostingEventHandler(final ProcessJobPostingCreatedPort processJobPostingCreatedPort) {
    this.processJobPostingCreatedPort = processJobPostingCreatedPort;
  }

  @Override
  public JobPostingEventType type() {
    return JobPostingEventType.ANALYSIS;
  }

  @Override
  public void handle(final JobPostingCreated event) {
    processJobPostingCreatedPort.analyzePosting(event);
  }
}
