package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import dev.jpje.jobtracker.api.dto.JobAnalysisResponse;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.port.inbound.ManageJobAnalysisPort;
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
class JobAnalysisQueryResolverTest {

  @InjectMocks
  private JobAnalysisQueryResolver resolver;

  @Mock
  private ManageJobAnalysisPort useCase;

  private static Stream<Arguments> notFoundScenarios() {
    return Stream.of(
      arguments(named("missing analysis", UUID.randomUUID())),
      arguments(named("another user's analysis", UUID.randomUUID()))
    );
  }

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

  @Test
  void shouldResolveOwnedAnalysis() {
    // Given
    final var userId = UserId.generate();
    final var analysis = analysisRecord(userId);
    when(useCase.findByIdForUser(userId, analysis.id())).thenReturn(Optional.of(analysis));

    // When
    final var result = resolver.analysis(userId, analysis.id());

    // Then
    assertThat(result)
      .as("owned analysis should be resolved")
      .isEqualTo(JobAnalysisResponse.from(analysis));
    verify(useCase, description("analysis lookup should be scoped to the caller")).findByIdForUser(userId, analysis.id());
    verifyNoMoreInteractions(useCase);
  }

  @ParameterizedTest(name = "{0} surfaces NOT_FOUND")
  @MethodSource("notFoundScenarios")
  void shouldThrowNotFoundWhenAnalysisNotAccessible(final UUID id) {
    // Given
    final var userId = UserId.generate();
    when(useCase.findByIdForUser(userId, id)).thenReturn(Optional.empty());

    // When, then
    assertThatThrownBy(() -> resolver.analysis(userId, id))
      .as("an analysis that is missing or not owned should be indistinguishable")
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("Analysis not found");
    verify(useCase, description("inaccessible analysis lookup should surface not found")).findByIdForUser(userId, id);
    verifyNoMoreInteractions(useCase);
  }

  @Test
  void shouldRejectAnalysisWithoutAuthentication() {
    // Given
    final var id = UUID.randomUUID();

    // When, then
    assertThatThrownBy(() -> resolver.analysis(null, id))
      .isInstanceOf(NullPointerException.class)
      .hasMessage("Authentication required");
    verifyNoMoreInteractions(useCase);
  }
}
