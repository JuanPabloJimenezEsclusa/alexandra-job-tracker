package dev.jpje.jobtracker.domain.service;

import java.time.Clock;

import dev.jpje.jobtracker.domain.event.EventPublisher;
import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobPosting;

public class JobPostingService {
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public JobPostingService(final EventPublisher eventPublisher, final Clock clock) {
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  public void submit(final JobPosting jobPosting) {
    eventPublisher.publish(JobPostingCreated.of(jobPosting, clock.instant()));
  }
}
