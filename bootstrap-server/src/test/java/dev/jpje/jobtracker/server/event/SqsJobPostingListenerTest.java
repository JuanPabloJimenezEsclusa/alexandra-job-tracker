package dev.jpje.jobtracker.server.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Consumer;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.awspring.cloud.sqs.listener.SqsHeaders;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.support.MessageBuilder;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@ExtendWith(MockitoExtension.class)
class SqsJobPostingListenerTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"),
    ZoneOffset.UTC);
  private static final String QUEUE_URL = "https://sqs.test/queue";
  private static final String RECEIPT_HANDLE = "receipt-handle";

  @Mock
  private JobPostingEventProcessor eventProcessor;

  @Mock
  private ObjectProvider<SqsTemplate> sqsTemplateProvider;

  @Mock
  private ObjectProvider<SqsAsyncClient> sqsAsyncClientProvider;

  @Mock
  private SqsAsyncClient sqsAsyncClient;

  private SqsJobPostingListener listener;

  @BeforeEach
  void setUp() {
    listener = new SqsJobPostingListener(eventProcessor, "sns", QUEUE_URL,
      sqsTemplateProvider, sqsAsyncClientProvider);
  }

  @Test
  void shouldProcessAndDeleteMessage() {
    final var event = event();
    final var message = message(event);

    listener.processMessage(message, sqsAsyncClient);

    verify(eventProcessor, description("event processed")).process(event);
    verify(sqsAsyncClient, description("message deleted")).deleteMessage(any(Consumer.class));
  }

  @Test
  void shouldDeleteDuplicateMessage() {
    final var event = event();
    final var message = message(event);
    doThrow(new ResourceAlreadyExistsException("Application already exists"))
      .when(eventProcessor).process(event);

    listener.processMessage(message, sqsAsyncClient);

    verify(sqsAsyncClient, description("duplicate message deleted")).deleteMessage(any(Consumer.class));
  }

  @Test
  void shouldNotDeleteMessageWhenProcessingFails() {
    final var event = event();
    final var message = message(event);
    doThrow(new IllegalStateException("boom")).when(eventProcessor).process(event);

    listener.processMessage(message, sqsAsyncClient);

    verify(sqsAsyncClient, never()).deleteMessage(any(Consumer.class));
    verify(eventProcessor, description("event processing attempted")).process(event);
  }

  private static org.springframework.messaging.Message<JobPostingCreated> message(final JobPostingCreated event) {
    return MessageBuilder.withPayload(event)
      .setHeader(SqsHeaders.SQS_RECEIPT_HANDLE_HEADER, RECEIPT_HANDLE)
      .build();
  }

  private static JobPostingCreated event() {
    final var posting = new JobPosting(UUID.randomUUID(), UserId.generate(),
      Url.of("https://example.com/job"), Source.LINKEDIN, JobTitle.of("Engineer"),
      CompanyName.of("Acme"), "Java developer with Spring experience", FIXED_CLOCK.instant());
    return JobPostingCreated.of(posting, FIXED_CLOCK.instant());
  }
}
