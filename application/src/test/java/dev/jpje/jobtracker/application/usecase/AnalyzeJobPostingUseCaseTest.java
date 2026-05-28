package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.outbound.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyzeJobPostingUseCaseTest {

  @Spy
  private final Clock fixedClock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

  @Mock
  private LoadJobPostingPort loadPort;

  @Mock
  private JobAnalysisPort analysisPort;

  @Mock
  private SaveJobAnalysisPort savePort;

  @InjectMocks
  private AnalyzeJobPostingUseCase useCase;

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

  @Test
  void shouldAnalyzeAndPersistPosting() {
    // Given
    final var userId = UserId.generate();
    final var posting = Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), userId)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .set(field(JobPosting::url), Url.of("https://example.com/job"))
      .set(field(JobPosting::title), JobTitle.of("Software Engineer"))
      .set(field(JobPosting::company), CompanyName.of("Acme"))
      .set(field(JobPosting::description), "We need a Java developer with Spring experience")
      .create();
    final var expectedAnalysis = validAnalysis();
    when(loadPort.findByIdAndUser(posting.id(), userId)).thenReturn(Optional.of(posting));
    when(analysisPort.analyze(posting.title().value(), posting.company().value(),
      posting.source().name(), posting.description())).thenReturn(expectedAnalysis);

    // When, Then
    assertThat(useCase.analyze(posting.userId(), posting.id()))
      .extracting(JobAnalysisRecord::jobPostingId, JobAnalysisRecord::userId,
        JobAnalysisRecord::analysis, JobAnalysisRecord::createdAt)
      .containsExactly(posting.id(), posting.userId(), expectedAnalysis, fixedClock.instant());
    verify(savePort).saveOrReplace(any(JobAnalysisRecord.class));
    verifyNoMoreInteractions(loadPort, analysisPort, savePort);
  }

  private static Stream<Arguments> notAccessibleScenarios() {
    return Stream.of(
      arguments(named("missing posting", UUID.randomUUID())),
      arguments(named("another user's posting", UUID.randomUUID()))
    );
  }

  @ParameterizedTest(name = "{0} rejects analysis with NOT_FOUND")
  @MethodSource("notAccessibleScenarios")
  void shouldRejectAnalysisWhenPostingNotAccessible(final UUID id) {
    // Given
    final var userId = UserId.generate();
    when(loadPort.findByIdAndUser(id, userId)).thenReturn(Optional.empty());

    // When, then
    assertThatThrownBy(() -> useCase.analyze(userId, id))
      .as("a posting that is missing or not owned should be indistinguishable")
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("Job posting not found");
    verify(savePort, never()).saveOrReplace(any());
    verifyNoMoreInteractions(loadPort, analysisPort, savePort);
  }
}
