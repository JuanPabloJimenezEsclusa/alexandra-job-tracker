package dev.jpje.jobtracker.events.handler;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;

public interface JobPostingEventHandler {
  JobPostingEventType type();

  void handle(JobPostingCreated event);
}
