package dev.jpje.jobtracker.events.handler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class JobPostingEventHandlerFactory {
  private final Map<JobPostingEventType, JobPostingEventHandler> handlers;

  public JobPostingEventHandlerFactory(final List<JobPostingEventHandler> handlers) {
    this.handlers = handlers.stream()
      .collect(Collectors.toMap(JobPostingEventHandler::type, Function.identity()));
  }

  public JobPostingEventHandler get(final JobPostingEventType type) {
    return handlers.get(type);
  }
}
