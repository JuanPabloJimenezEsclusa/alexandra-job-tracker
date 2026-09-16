package dev.jpje.jobtracker.events.api;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.events.handler.JobPostingEventType;
import dev.jpje.jobtracker.events.handler.JobPostingEventHandlerFactory;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@RestController
@Profile("aws")
@RequestMapping("/events/sqs")
public class SqsJobEventReceiver {
  private static final Logger log = LoggerFactory.getLogger(SqsJobEventReceiver.class);
  private static final String TRACKING_QUEUE_MARKER = "ajt-job-tracking";
  private static final String ANALYSIS_QUEUE_MARKER = "ajt-job-analysis";

  private final JobPostingEventHandlerFactory handlerFactory;
  private final ObjectMapper objectMapper;

  public SqsJobEventReceiver(final JobPostingEventHandlerFactory handlerFactory,
                             final ObjectMapper objectMapper) {
    this.handlerFactory = handlerFactory;
    this.objectMapper = objectMapper;
  }

  @PostMapping
  public ResponseEntity<Void> receive(@RequestBody final SqsEventEnvelope envelope) {
    for (final SqsEventRecord sqsEvent : envelope.records()) {
      final var status = processRecord(sqsEvent);
      if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
    }
    return ResponseEntity.ok().build();
  }

  private HttpStatus processRecord(final SqsEventRecord sqsEvent) {
    final JobPostingCreated event;
    try {
      event = objectMapper.readValue(sqsEvent.body(), JobPostingCreated.class);
    } catch (final Exception e) {
      log.error("Failed to deserialize SQS record from {}: {}", sqsEvent.eventSourceArn(), e.getMessage());
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
    final var type = typeFor(sqsEvent.eventSourceArn());
    if (type == null) {
      log.warn("Ignoring SQS message from unrecognized queue {}", sqsEvent.eventSourceArn());
      return HttpStatus.OK;
    }
    try {
      log.info("Received JobPostingCreated from {} queue {}: {}", type, sqsEvent.eventSourceArn(), event);
      handlerFactory.get(type).handle(event);
      return HttpStatus.OK;
    } catch (final ResourceAlreadyExistsException e) {
      log.warn("Ignoring duplicate SQS message from {}: {}", sqsEvent.eventSourceArn(), e.getMessage());
      return HttpStatus.OK;
    } catch (final RuntimeException e) {
      log.error("Failed to process SQS record from {}", sqsEvent.eventSourceArn(), e);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }

  private static @Nullable JobPostingEventType typeFor(final @Nullable String arn) {
    if (arn == null) {
      return null;
    }
    if (arn.contains(TRACKING_QUEUE_MARKER)) {
      return JobPostingEventType.TRACKING;
    }
    if (arn.contains(ANALYSIS_QUEUE_MARKER)) {
      return JobPostingEventType.ANALYSIS;
    }
    return null;
  }
}
