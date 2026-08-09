package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnalyzeJobPostingUseCaseTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.now().plusSeconds(60L),
    ZoneOffset.UTC);

  private static JobAnalysis validAnalysis() {
    return Instancio.of(JobAnalysis.class)
      .set(field(JobAnalysis::fitScore), 85.0)
      .set(field(JobAnalysis::companyRating), 4.2)
      .set(field(JobAnalysis::companyType), "enterprise")
      .set(field(JobAnalysis::salaryMin), 90000.0)
      .set(field(JobAnalysis::salaryMax), 130000.0)
      .set(field(JobAnalysis::salaryCurrency), "USD")
      .create();
  }

  private static Stream<Arguments> analysisScenarios() {
    final var userId = UserId.generate();
    final var posting = Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), userId)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .set(field(JobPosting::url), Url.of("https://example.com/job"))
      .set(field(JobPosting::title), JobTitle.of("Software Engineer"))
      .set(field(JobPosting::company), CompanyName.of("Acme"))
      .set(field(JobPosting::description), "We need a Java developer with Spring experience")
      .create();
    return Stream.of(
      arguments(named("found posting", posting), validAnalysis())
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("analysisScenarios")
  void shouldAnalyzeAndPersistPosting(final JobPosting posting, final JobAnalysis expectedAnalysis) {
    // Given
    final var loadPort = mock(LoadJobPostingPort.class);
    final var analysisPort = mock(JobAnalysisPort.class);
    final var savePort = mock(SaveJobAnalysisPort.class);
    final var useCase = new AnalyzeJobPostingUseCase(loadPort, analysisPort, savePort, FIXED_CLOCK);

    when(loadPort.findById(posting.id())).thenReturn(Optional.of(posting));
    when(analysisPort.analyze(posting.description())).thenReturn(expectedAnalysis);

    // When
    final var result = useCase.analyze(posting.userId(), posting.id());

    // Then
    assertThat(result)
      .extracting(JobAnalysisRecord::jobPostingId, JobAnalysisRecord::userId,
        JobAnalysisRecord::analysis, JobAnalysisRecord::createdAt)
      .containsExactly(posting.id(), posting.userId(), expectedAnalysis, FIXED_CLOCK.instant());
    verify(savePort).saveOrReplace(any(JobAnalysisRecord.class));
  }

  @Test
  void shouldThrowWhenPostingNotFound() {
    // Given
    final var loadPort = mock(LoadJobPostingPort.class);
    final var analysisPort = mock(JobAnalysisPort.class);
    final var savePort = mock(SaveJobAnalysisPort.class);
    final var useCase = new AnalyzeJobPostingUseCase(loadPort, analysisPort, savePort, FIXED_CLOCK);
    final var userId = UserId.generate();
    final var randomUUID = UUID.randomUUID();

    when(loadPort.findById(randomUUID)).thenReturn(Optional.empty());

    // When, then
    assertThatThrownBy(() -> useCase.analyze(userId, randomUUID))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("Job posting not found");
    verify(savePort, never()).saveOrReplace(any());
  }
}
