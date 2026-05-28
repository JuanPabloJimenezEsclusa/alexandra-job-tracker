package com.jobtracker.application.service;

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

class AnalyzeJobPostingUseCaseImplTest {

  private static Stream<Arguments> analysisScenarios() {
    var jobPostingId = UUID.randomUUID();
    var userId = UserId.generate();
    var posting = new JobPosting(jobPostingId, userId, "https://example.com/job",
      Source.LINKEDIN, "Software Engineer", "Acme",
      "We need a Java developer with Spring experience", Instant.now());
    var analysis = new JobAnalysis("Java role", List.of("Java", "Spring"), 90.0);
    return Stream.of(
      arguments(named("found posting", posting), analysis),
      arguments(named("missing posting", null), null)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("analysisScenarios")
  void shouldAnalyzeOrThrow(final JobPosting posting, final JobAnalysis expectedAnalysis) {
    // Given
    final var loadPort = mock(LoadJobPostingPort.class);
    final var analysisPort = mock(JobAnalysisPort.class);
    final var useCase = new AnalyzeJobPostingUseCaseImpl(loadPort, analysisPort);
    final var randomUUID = UUID.randomUUID();

    if (posting != null) {
      when(loadPort.findById(posting.id())).thenReturn(Optional.of(posting));
      when(analysisPort.analyze(posting.description())).thenReturn(expectedAnalysis);
    } else {
      when(loadPort.findById(randomUUID)).thenReturn(Optional.empty());
    }

    if (posting == null) {
      // When, Then
      assertThatThrownBy(() -> useCase.analyze(randomUUID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Job posting not found");
      return;
    }

    // When
    final var result = useCase.analyze(posting.id());

    // Then
    assertThat(result).isEqualTo(expectedAnalysis);
    assertThat(result.summary()).isEqualTo("Java role");
    assertThat(result.skills()).contains("Java", "Spring");
    assertThat(result.fitScore()).isEqualTo(90.0);
  }
}
