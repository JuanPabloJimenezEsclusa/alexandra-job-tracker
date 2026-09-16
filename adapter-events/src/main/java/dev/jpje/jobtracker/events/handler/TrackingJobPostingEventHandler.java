package dev.jpje.jobtracker.events.handler;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.port.inbound.ProcessJobPostingCreatedPort;
import org.springframework.stereotype.Component;

@Component
public class TrackingJobPostingEventHandler implements JobPostingEventHandler {
  private final ProcessJobPostingCreatedPort processJobPostingCreatedPort;

  public TrackingJobPostingEventHandler(final ProcessJobPostingCreatedPort processJobPostingCreatedPort) {
    this.processJobPostingCreatedPort = processJobPostingCreatedPort;
  }

  @Override
  public JobPostingEventType type() {
    return JobPostingEventType.TRACKING;
  }

  @Override
  public void handle(final JobPostingCreated event) {
    processJobPostingCreatedPort.createTracking(event);
  }
}
