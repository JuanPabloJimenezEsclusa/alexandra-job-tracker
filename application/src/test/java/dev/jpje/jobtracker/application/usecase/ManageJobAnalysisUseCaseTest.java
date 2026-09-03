package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.port.out.LoadJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageJobAnalysisUseCaseTest {

  @Mock
  private LoadJobAnalysisPort loadPort;

  @Mock
  private SaveJobAnalysisPort savePort;

  @InjectMocks
  private ManageJobAnalysisUseCase useCase;

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

  private static JobAnalysisRecord analysisRecord(final UserId userId) {
    return Instancio.of(JobAnalysisRecord.class)
      .set(field(JobAnalysisRecord::jobPostingId), UUID.randomUUID())
      .set(field(JobAnalysisRecord::userId), userId)
      .set(field(JobAnalysisRecord::analysis), validAnalysis())
      .set(field(JobAnalysisRecord::createdAt), java.time.Instant.EPOCH)
      .create();
  }

  private static Stream<Arguments> emptyScenarios() {
    return Stream.of(
      arguments(named("missing analysis", UUID.randomUUID())),
      arguments(named("another user's analysis", UUID.randomUUID()))
    );
  }

  @Test
  void shouldReturnAnalysisOwnedByCaller() {
    // Given
    final var userId = UserId.generate();
    final var analysisRecord = analysisRecord(userId);
    when(loadPort.findByIdAndUser(analysisRecord.id(), userId)).thenReturn(Optional.of(analysisRecord));

    // When
    final var result = useCase.findByIdForUser(userId, analysisRecord.id());

    // Then
    assertThat(result).as("owned analysis should be returned").contains(analysisRecord);
    verify(loadPort, description("owned analysis lookup should hit")).findByIdAndUser(analysisRecord.id(), userId);
    verifyNoMoreInteractions(loadPort, savePort);
  }

  @ParameterizedTest(name = "{0} is indistinguishable from missing")
  @MethodSource("emptyScenarios")
  void shouldReturnEmptyWhenAnalysisNotAccessible(final UUID id) {
    // Given
    final var userId = UserId.generate();
    when(loadPort.findByIdAndUser(id, userId)).thenReturn(Optional.empty());

    // When
    final var result = useCase.findByIdForUser(userId, id);

    // Then
    assertThat(result).as("analysis not owned or missing should be empty").isEmpty();
    verify(loadPort, description("scoped lookup should miss for the caller")).findByIdAndUser(id, userId);
    verifyNoMoreInteractions(loadPort, savePort);
  }
}
