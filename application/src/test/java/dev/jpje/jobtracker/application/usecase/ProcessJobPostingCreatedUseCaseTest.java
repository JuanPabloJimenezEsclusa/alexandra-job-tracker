package dev.jpje.jobtracker.application.usecase;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProcessJobPostingCreatedUseCaseTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"),
    ZoneOffset.UTC);

  @Mock
  private SaveJobApplicationPort saveApplicationPort;

  @Mock
  private JobAnalysisPort analysisPort;

  @Mock
  private SaveJobAnalysisPort saveAnalysisPort;

  private ProcessJobPostingCreatedUseCase useCase;

  @BeforeEach
  void setUp() {
    useCase = new ProcessJobPostingCreatedUseCase(saveApplicationPort, analysisPort, saveAnalysisPort,
      FIXED_CLOCK);
  }

  @Test
  void shouldCreateTrackingIndependentlyOfAnalysis() {
    final var event = event();

    useCase.createTracking(event);

    verify(saveApplicationPort, description("tracking application saved")).save(any(JobApplication.class));
    verifyNoInteractions(analysisPort, saveAnalysisPort);
  }

  @Test
  void shouldAnalyzePostingIndependentlyOfTracking() {
    final var event = event();
    final var posting = event.jobPosting();
    final var jobAnalysis = mock(JobAnalysis.class);
    when(analysisPort.analyze(posting.title().value(), posting.company().value(),
      posting.source().name(), posting.description())).thenReturn(jobAnalysis);

    useCase.analyzePosting(event);

    verify(saveAnalysisPort, description("analysis persisted")).saveOrReplace(any());
    verifyNoInteractions(saveApplicationPort);
  }

  private static JobPostingCreated event() {
    final var posting = new JobPosting(UUID.randomUUID(), UserId.generate(),
      Url.of("https://example.com/job"), Source.LINKEDIN, JobTitle.of("Engineer"),
      CompanyName.of("Acme"), "Java developer with Spring experience", FIXED_CLOCK.instant());
    return JobPostingCreated.of(posting, FIXED_CLOCK.instant());
  }
}
