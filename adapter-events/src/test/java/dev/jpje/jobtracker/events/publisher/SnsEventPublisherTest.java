package dev.jpje.jobtracker.events.publisher;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.awspring.cloud.sns.core.SnsNotification;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SnsEventPublisherTest {

  private static final String TOPIC_ARN = "arn:aws:sns:eu-west-1:123456789012:ajt-job-events";

  @Mock
  private SnsTemplate snsTemplate;

  @Test
  void shouldSendNotificationToTheConfiguredTopic() {
    final var publisher = new SnsEventPublisher(snsTemplate, TOPIC_ARN);

    publisher.publish(event());

    verify(snsTemplate).sendNotification(eq(TOPIC_ARN), ArgumentMatchers.<SnsNotification<?>>any());
  }

  private static JobPostingCreated event() {
    final var posting = new JobPosting(UUID.randomUUID(), UserId.generate(),
      Url.of("https://example.com/job"), Source.LINKEDIN, JobTitle.of("Engineer"),
      CompanyName.of("Acme"), "Java developer with Spring experience", Instant.now());
    return JobPostingCreated.of(posting, Instant.now());
  }
}
