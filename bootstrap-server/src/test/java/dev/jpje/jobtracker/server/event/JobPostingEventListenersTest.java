package dev.jpje.jobtracker.server.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.outbound.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.micrometer.core.instrument.Counter;
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
  private SaveJobApplicationPort saveAppPort;

  @Mock
  private JobAnalysisPort analysisPort;

  @Mock
  private SaveJobAnalysisPort saveAnalysisPort;

  @Mock
  private Counter applicationCreatedCounter;

  private JobPostingEventListeners listeners;

  @BeforeEach
  void setUp() {
    listeners = new JobPostingEventListeners(saveAppPort, analysisPort, saveAnalysisPort,
      FIXED_CLOCK, applicationCreatedCounter, "spring");
  }

  @Test
  void shouldCreateTrackingOnPostingCreated() {
    final var event = event();

    listeners.createTracking(event);

    verify(saveAppPort, description("tracking application saved")).save(any(JobApplication.class));
    verify(applicationCreatedCounter, description("creation counter incremented")).increment();
  }

  @Test
  void shouldAnalyzePostingOnPostingCreated() {
    final var event = event();
    final var posting = event.jobPosting();
    final var analysis = mock(JobAnalysis.class);
    when(analysisPort.analyze(posting.title().value(), posting.company().value(),
      posting.source().name(), posting.description())).thenReturn(analysis);

    listeners.analyzePosting(event);

    verify(analysisPort, description("posting analyzed via AI port")).analyze(posting.title().value(),
      posting.company().value(), posting.source().name(), posting.description());
    verify(saveAnalysisPort, description("analysis persisted")).saveOrReplace(any());
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
