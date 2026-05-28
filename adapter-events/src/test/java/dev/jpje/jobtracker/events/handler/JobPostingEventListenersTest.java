package dev.jpje.jobtracker.events.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobPostingEventListenersTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"),
    ZoneOffset.UTC);

  @Mock
  private JobPostingEventHandlerFactory handlerFactory;

  @Mock
  private JobPostingEventHandler trackingHandler;

  @Mock
  private JobPostingEventHandler analysisHandler;

  private JobPostingEventListeners listeners;

  @BeforeEach
  void setUp() {
    listeners = new JobPostingEventListeners(handlerFactory);
  }

  @Test
  void shouldCreateTrackingOnPostingCreated() {
    when(handlerFactory.get(JobPostingEventType.TRACKING)).thenReturn(trackingHandler);

    listeners.createTracking(event());

    verify(trackingHandler, description("tracking handled")).handle(any());
  }

  @Test
  void shouldAnalyzePostingOnPostingCreated() {
    when(handlerFactory.get(JobPostingEventType.ANALYSIS)).thenReturn(analysisHandler);

    listeners.analyzePosting(event());

    verify(analysisHandler, description("analysis handled")).handle(any());
  }

  private static JobPostingCreated event() {
    final var posting = new JobPosting(
      UUID.randomUUID(),
      UserId.generate(),
      Url.of("https://example.com/job"),
      Source.LINKEDIN,
      JobTitle.of("Engineer"),
      CompanyName.of("Acme"),
      "Java developer with Spring experience",
      FIXED_CLOCK.instant());
    return JobPostingCreated.of(posting, FIXED_CLOCK.instant());
  }
}
