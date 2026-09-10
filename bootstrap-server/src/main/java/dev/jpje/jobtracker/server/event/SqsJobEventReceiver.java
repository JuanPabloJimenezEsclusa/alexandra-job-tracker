package dev.jpje.jobtracker.server.event;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
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

  private final JobPostingEventProcessor eventProcessor;
  private final ObjectMapper objectMapper;

  public SqsJobEventReceiver(final JobPostingEventProcessor eventProcessor, final ObjectMapper objectMapper) {
    this.eventProcessor = eventProcessor;
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
    try {
      final var arn = sqsEvent.eventSourceArn();
      if (arn != null && arn.contains(TRACKING_QUEUE_MARKER)) {
        log.info("Received JobPostingCreated from tracking queue {}: {}", arn, event);
        eventProcessor.createTracking(event);
      } else if (arn != null && arn.contains(ANALYSIS_QUEUE_MARKER)) {
        log.info("Received JobPostingCreated from analysis queue {}: {}", arn, event);
        eventProcessor.analyzePosting(event);
      } else {
        log.warn("Ignoring SQS message from unrecognized queue {}", arn);
      }
      return HttpStatus.OK;
    } catch (final ResourceAlreadyExistsException e) {
      log.warn("Ignoring duplicate SQS message from {}: {}", sqsEvent.eventSourceArn(), e.getMessage());
      return HttpStatus.OK;
    } catch (final RuntimeException e) {
      log.error("Failed to process SQS record from {}", sqsEvent.eventSourceArn(), e);
      return HttpStatus.INTERNAL_SERVER_ERROR;
    }
  }
}
