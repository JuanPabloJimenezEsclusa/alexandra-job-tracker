package dev.jpje.jobtracker.server.event;

import java.time.Duration;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import io.awspring.cloud.sqs.listener.SqsHeaders;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Component
public class SqsJobPostingListener {
  private static final Logger log = LoggerFactory.getLogger(SqsJobPostingListener.class);
  private static final Duration POLL_TIMEOUT = Duration.ofSeconds(10);
  private static final Duration POLL_ERROR_BACKOFF = Duration.ofSeconds(2);
  private static final String SNS_TRANSPORT = "sns";
  private static final String POLLER_THREAD_NAME = "ajt-sqs-poller";

  private final JobPostingEventProcessor eventProcessor;
  private final String transport;
  private final String queueUrl;
  private final ObjectProvider<SqsTemplate> sqsTemplateProvider;
  private final ObjectProvider<SqsAsyncClient> sqsAsyncClientProvider;

  @Nullable
  private Thread pollerThread;

  public SqsJobPostingListener(final JobPostingEventProcessor eventProcessor,
                               @Value("${ajt.events.transport:spring}") final String transport,
                               @Value("${ajt.events.sqs-queue:}") final String queueUrl,
                               final ObjectProvider<SqsTemplate> sqsTemplateProvider,
                               final ObjectProvider<SqsAsyncClient> sqsAsyncClientProvider) {
    this.eventProcessor = eventProcessor;
    this.transport = transport;
    this.queueUrl = queueUrl;
    this.sqsTemplateProvider = sqsTemplateProvider;
    this.sqsAsyncClientProvider = sqsAsyncClientProvider;
  }

  @PostConstruct
  void startPolling() {
    if (!SNS_TRANSPORT.equals(transport)) {
      return;
    }
    final var sqsTemplate = sqsTemplateProvider.getIfAvailable();
    final var sqsAsyncClient = sqsAsyncClientProvider.getIfAvailable();
    if (sqsTemplate == null || sqsAsyncClient == null || queueUrl.isBlank()) {
      log.warn("SQS polling disabled: sqsTemplate={} sqsAsyncClient={} queueUrl='{}'",
        sqsTemplate != null, sqsAsyncClient != null, queueUrl);
      return;
    }
    pollerThread = Thread.ofVirtual().name(POLLER_THREAD_NAME).start(() -> pollLoop(sqsTemplate, sqsAsyncClient));
  }

  @PreDestroy
  void stopPolling() {
    if (pollerThread != null) {
      pollerThread.interrupt();
    }
  }

  private void pollLoop(final SqsTemplate sqsTemplate, final SqsAsyncClient sqsAsyncClient) {
    while (!Thread.currentThread().isInterrupted()) {
      pollOnce(sqsTemplate, sqsAsyncClient);
    }
  }

  private void pollOnce(final SqsTemplate sqsTemplate, final SqsAsyncClient sqsAsyncClient) {
    try {
      final var message = sqsTemplate.receive(
        options -> options.queue(queueUrl).pollTimeout(POLL_TIMEOUT), JobPostingCreated.class);
      message.ifPresent(received -> processMessage(received, sqsAsyncClient));
    } catch (final RuntimeException e) {
      log.error("Error polling SQS queue {}", queueUrl, e);
      sleepOnPollError();
    }
  }

  void processMessage(final Message<JobPostingCreated> received, final SqsAsyncClient sqsAsyncClient) {
    try {
      eventProcessor.process(received.getPayload());
      deleteMessage(received, sqsAsyncClient);
    } catch (final ResourceAlreadyExistsException e) {
      log.warn("Ignoring duplicate SQS message from {}: {}", queueUrl, e.getMessage());
      deleteMessage(received, sqsAsyncClient);
    } catch (final RuntimeException e) {
      log.error("Failed to process SQS message from {}", queueUrl, e);
    }
  }

  private void sleepOnPollError() {
    try {
      Thread.sleep(POLL_ERROR_BACKOFF.toMillis());
    } catch (final InterruptedException _) {
      Thread.currentThread().interrupt();
    }
  }

  private void deleteMessage(final Message<JobPostingCreated> received, final SqsAsyncClient sqsAsyncClient) {
    final var receipt = received.getHeaders().get(SqsHeaders.SQS_RECEIPT_HANDLE_HEADER, String.class);
    if (receipt != null) {
      sqsAsyncClient.deleteMessage(builder -> builder.queueUrl(queueUrl).receiptHandle(receipt));
    }
  }
}
