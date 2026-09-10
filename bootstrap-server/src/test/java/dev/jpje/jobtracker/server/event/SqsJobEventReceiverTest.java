package dev.jpje.jobtracker.server.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

  @Mock
  private JobPostingEventProcessor eventProcessor;

  private SqsJobEventReceiver receiver;
  private final ObjectMapper mapper = new ObjectMapper();

  @BeforeEach
  void setUp() {
    receiver = new SqsJobEventReceiver(eventProcessor, mapper);
  }

  @Test
  void shouldRouteTrackingRecordToCreateTracking() {
    final var envelope = envelope(TRACKING_ARN);

    final var response = receiver.receive(envelope);

    assertThat(response.getStatusCode()).as("tracking event accepted").isEqualTo(HttpStatus.OK);
    verify(eventProcessor, description("tracking created")).createTracking(any());
    verify(eventProcessor, never()).analyzePosting(any());
  }

  @Test
  void shouldRouteAnalysisRecordToAnalyzePosting() {
    final var envelope = envelope(ANALYSIS_ARN);

    final var response = receiver.receive(envelope);

    assertThat(response.getStatusCode()).as("analysis event accepted").isEqualTo(HttpStatus.OK);
    verify(eventProcessor, description("posting analyzed")).analyzePosting(any());
    verify(eventProcessor, never()).createTracking(any());
  }

  @Test
  void shouldAcknowledgeDuplicateTrackingMessage() {
    final var envelope = envelope(TRACKING_ARN);
    doThrow(new ResourceAlreadyExistsException("Application already exists"))
      .when(eventProcessor).createTracking(any());

    final var response = receiver.receive(envelope);

    assertThat(response.getStatusCode()).as("duplicate acknowledged").isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldFailInvocationOnProcessingError() {
    final var envelope = envelope(ANALYSIS_ARN);
    doThrow(new IllegalStateException("boom")).when(eventProcessor).analyzePosting(any());

    final var response = receiver.receive(envelope);

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
