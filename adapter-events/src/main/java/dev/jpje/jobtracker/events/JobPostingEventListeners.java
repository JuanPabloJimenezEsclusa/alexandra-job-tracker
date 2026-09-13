package dev.jpje.jobtracker.events;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Profile("!aws")
public class JobPostingEventListeners {
  private static final Logger log = LoggerFactory.getLogger(JobPostingEventListeners.class);

  private final JobPostingEventHandlerFactory handlerFactory;

  public JobPostingEventListeners(final JobPostingEventHandlerFactory handlerFactory) {
    this.handlerFactory = handlerFactory;
  }

  @Async
  @TransactionalEventListener
  public void createTracking(final JobPostingCreated event) {
    log.debug("createTracking for posting {}", event);
    handlerFactory.get(JobPostingEventType.TRACKING).handle(event);
  }

  @Async
  @TransactionalEventListener
  public void analyzePosting(final JobPostingCreated event) {
    log.debug("analyzePosting for posting {}", event);
    handlerFactory.get(JobPostingEventType.ANALYSIS).handle(event);
  }
}
