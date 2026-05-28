package dev.jpje.jobtracker.server.event;

import java.time.Clock;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class JobPostingEventListeners {
  private static final Logger log = LoggerFactory.getLogger(JobPostingEventListeners.class);

  private final SaveJobApplicationPort saveAppPort;
  private final JobAnalysisPort analysisPort;
  private final SaveJobAnalysisPort saveAnalysisPort;
  private final Clock clock;
  private final Counter applicationCreatedCounter;
  private final String transport;

  public JobPostingEventListeners(final SaveJobApplicationPort saveAppPort,
                                  final JobAnalysisPort analysisPort,
                                  final SaveJobAnalysisPort saveAnalysisPort,
                                  final Clock clock,
                                  final Counter applicationCreatedCounter,
                                  @Value("${ajt.events.transport:spring}") final String transport) {
    this.saveAppPort = saveAppPort;
    this.analysisPort = analysisPort;
    this.saveAnalysisPort = saveAnalysisPort;
    this.clock = clock;
    this.applicationCreatedCounter = applicationCreatedCounter;
    this.transport = transport;
  }

  @Async
  @TransactionalEventListener
  public void createTracking(final JobPostingCreated event) {
    if ("sns".equals(transport)) {
      return;
    }
    log.debug("createTracking for posting {}", event);
    JobPostingEventHelper.createTracking(saveAppPort, applicationCreatedCounter, event, clock.instant());
  }

  @Async
  @TransactionalEventListener
  public void analyzePosting(final JobPostingCreated event) {
    if ("sns".equals(transport)) {
      return;
    }
    log.debug("analyzePosting for posting {}", event);
    JobPostingEventHelper.analyzePosting(analysisPort, saveAnalysisPort, event, clock.instant());
  }
}
