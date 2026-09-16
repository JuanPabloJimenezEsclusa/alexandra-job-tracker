package dev.jpje.jobtracker.events.publisher;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class SpringEventPublisherTest {

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @Test
  void shouldPublishToTheSpringApplicationEventPublisher() {
    final var publisher = new SpringEventPublisher(applicationEventPublisher);
    final var event = event();

    publisher.publish(event);

    verify(applicationEventPublisher).publishEvent(event);
  }

  private static JobPostingCreated event() {
    final var posting = new JobPosting(UUID.randomUUID(), UserId.generate(),
      Url.of("https://example.com/job"), Source.LINKEDIN, JobTitle.of("Engineer"),
      CompanyName.of("Acme"), "Java developer with Spring experience", Instant.now());
    return JobPostingCreated.of(posting, Instant.now());
  }
}
