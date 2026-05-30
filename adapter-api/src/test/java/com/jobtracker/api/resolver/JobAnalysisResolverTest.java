package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.jobtracker.domain.model.JobAnalysis;
import com.jobtracker.domain.port.in.AnalyzeJobPostingUseCase;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobAnalysisResolverTest {

  private static Stream<Arguments> resolverScenarios() {
    final var jobId = UUID.randomUUID();
    final var analysis = new JobAnalysis("Java role", List.of("Spring", "SQL"), 85.0);
    return Stream.of(
      Arguments.of(jobId, analysis, null),
      Arguments.of(jobId, null, "Job posting not found")
    );
  }

  @ParameterizedTest(name = "resolver scenario {index}")
  @MethodSource("resolverScenarios")
  void shouldResolveOrThrow(final UUID jobPostingId, final JobAnalysis analysis, final String errorMessage) {
    final var useCase = mock(AnalyzeJobPostingUseCase.class);
    final var resolver = new JobAnalysisResolver(useCase);

    if (errorMessage != null) {
      when(useCase.analyze(jobPostingId)).thenThrow(new IllegalArgumentException(errorMessage));
      assertThatThrownBy(() -> resolver.analyzeJobPosting(jobPostingId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(errorMessage);
      return;
    }

    when(useCase.analyze(jobPostingId)).thenReturn(analysis);
    final var result = resolver.analyzeJobPosting(jobPostingId);

    assertThat(result.summary()).isEqualTo("Java role");
    assertThat(result.skills()).contains("Spring", "SQL");
    assertThat(result.fitScore()).isEqualTo(85.0);
  }
}
