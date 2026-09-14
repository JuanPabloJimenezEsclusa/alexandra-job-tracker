package dev.jpje.jobtracker.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class SqsJobEventReceiverTest {

  private static final String TRACKING_ARN = "arn:aws:sqs:eu-west-1:123456789012:ajt-job-tracking";
  private static final String ANALYSIS_ARN = "arn:aws:sqs:eu-west-1:123456789012:ajt-job-analysis";
  private static final String UNKNOWN_ARN = "arn:aws:sqs:eu-west-1:123456789012:ajt-job-unknown";

  @Mock
  private JobPostingEventHandlerFactory handlerFactory;

  @Mock
  private JobPostingEventHandler trackingHandler;

  @Mock
  private JobPostingEventHandler analysisHandler;

  private SqsJobEventReceiver receiver;
  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    receiver = new SqsJobEventReceiver(handlerFactory, mapper);
  }

  @Test
  void shouldRouteTrackingRecordToTrackingHandler() {
    when(handlerFactory.get(JobPostingEventType.TRACKING)).thenReturn(trackingHandler);
    final var envelope = envelope(TRACKING_ARN);

    final var response = receiver.receive(envelope);

    assertThat(response.getStatusCode()).as("tracking event accepted").isEqualTo(HttpStatus.OK);
    verify(trackingHandler, description("tracking handled")).handle(any());
    verify(analysisHandler, never()).handle(any());
  }

  @Test
  void shouldRouteAnalysisRecordToAnalysisHandler() {
    when(handlerFactory.get(JobPostingEventType.ANALYSIS)).thenReturn(analysisHandler);
    final var envelope = envelope(ANALYSIS_ARN);

    final var response = receiver.receive(envelope);

    assertThat(response.getStatusCode()).as("analysis event accepted").isEqualTo(HttpStatus.OK);
    verify(analysisHandler, description("analysis handled")).handle(any());
    verify(trackingHandler, never()).handle(any());
  }

  @Test
  void shouldAcknowledgeUnrecognizedQueue() {
    final var response = receiver.receive(envelope(UNKNOWN_ARN));

    assertThat(response.getStatusCode()).as("unrecognized queue acknowledged").isEqualTo(HttpStatus.OK);
    verify(handlerFactory, never()).get(any());
  }

  @Test
  void shouldAcknowledgeDuplicateTrackingMessage() {
    when(handlerFactory.get(JobPostingEventType.TRACKING)).thenReturn(trackingHandler);
    doThrow(new ResourceAlreadyExistsException("Application already exists"))
      .when(trackingHandler).handle(any());

    final var response = receiver.receive(envelope(TRACKING_ARN));

    assertThat(response.getStatusCode()).as("duplicate acknowledged").isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldFailInvocationOnProcessingError() {
    when(handlerFactory.get(JobPostingEventType.ANALYSIS)).thenReturn(analysisHandler);
    doThrow(new IllegalStateException("boom")).when(analysisHandler).handle(any());

    final var response = receiver.receive(envelope(ANALYSIS_ARN));

    assertThat(response.getStatusCode())
      .as("transient failure marked as server error for retry")
      .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  void shouldFailInvocationWhenBodyCannotBeDeserialized() {
    final var envelope = new SqsEventEnvelope(
      List.of(new SqsEventRecord("{not-json", TRACKING_ARN)));

    final var response = receiver.receive(envelope);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static SqsEventEnvelope envelope(final String eventSourceArn) {
    return new SqsEventEnvelope(List.of(
      new SqsEventRecord("""
        {"jobPosting":{"id":"11111111-1111-1111-1111-111111111111",\
        "userId":{"value":"22222222-2222-2222-2222-222222222222"},\
        "url":{"value":"https://example.com/job"},\
        "source":"LINKEDIN","title":{"value":"Engineer"},\
        "company":{"value":"Acme"},\
        "description":"Java developer with Spring experience",\
        "postedAt":"2026-01-01T00:00:00Z"},\
        "occurredAt":"2026-01-01T00:00:00Z"}
        """, eventSourceArn)));
  }
}
