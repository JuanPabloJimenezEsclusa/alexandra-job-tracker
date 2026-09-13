package dev.jpje.jobtracker.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.inbound.ProcessJobPostingCreatedPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackingJobPostingEventHandlerTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"),
    ZoneOffset.UTC);

  @Mock
  private ProcessJobPostingCreatedPort processJobPostingCreatedPort;

  @Test
  void shouldDelegateTrackingToTheCorePort() {
    final var handler = new TrackingJobPostingEventHandler(processJobPostingCreatedPort);

    assertThat(handler.type()).isEqualTo(JobPostingEventType.TRACKING);
    handler.handle(event());

    verify(processJobPostingCreatedPort, description("tracking delegated to the core"))
      .createTracking(any());
  }

  private static JobPostingCreated event() {
    final var posting = new JobPosting(UUID.randomUUID(), UserId.generate(),
      Url.of("https://example.com/job"), Source.LINKEDIN, JobTitle.of("Engineer"),
      CompanyName.of("Acme"), "Java developer with Spring experience", FIXED_CLOCK.instant());
    return JobPostingCreated.of(posting, FIXED_CLOCK.instant());
  }
}
