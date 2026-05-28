package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import com.jobtracker.domain.model.JobAnalysis;
import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.out.JobAnalysisPort;
import com.jobtracker.domain.port.out.LoadJobPostingPort;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobAnalysisResolverTest {

  private static Stream<Arguments> resolverScenarios() {
    final var jobId = UUID.randomUUID();
    final var analysis = new JobAnalysis("Java role", List.of("Spring", "SQL"), 85.0);
    return Stream.of(
      arguments(named("successful analysis", jobId), analysis, null),
      arguments(named("posting not found", jobId), null, "not found")
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("resolverScenarios")
  void shouldResolveOrThrow(final UUID jobPostingId, final JobAnalysis analysis, final String errorMessage) {
    // Given
    final var loadPort = mock(LoadJobPostingPort.class);
    final var analysisPort = mock(JobAnalysisPort.class);
    final var resolver = new JobAnalysisResolver(loadPort, analysisPort);

    if (analysis != null) {
      final var userId = UserId.generate();
      when(loadPort.findById(jobPostingId)).thenReturn(Optional.of(
        new JobPosting(jobPostingId, userId, "url", Source.LINKEDIN,
          "SWE", "Acme", "Java role description", Instant.now())));
      when(analysisPort.analyze("Java role description")).thenReturn(analysis);
    } else {
      when(loadPort.findById(jobPostingId)).thenReturn(Optional.empty());
    }

    if (errorMessage != null) {
      // When, Then
      assertThatThrownBy(() -> resolver.analyzeJobPosting(jobPostingId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Job posting not found");
      return;
    }

    // When
    final var result = resolver.analyzeJobPosting(jobPostingId);

    // Then
    assertThat(result.summary()).isEqualTo("Java role");
    assertThat(result.skills()).contains("Spring", "SQL");
    assertThat(result.fitScore()).isEqualTo(85.0);
  }
}
