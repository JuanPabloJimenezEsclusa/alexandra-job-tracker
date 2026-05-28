package com.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobAnalysisTest {

  private static Stream<Arguments> validAnalysis() {
    return Stream.of(
      arguments(named("full analysis", "Great role"), List.of("Java", "Spring"), 85.0),
      arguments(named("empty analysis", ""), List.of(), 0.0),
      arguments(named("single skill", "Needs experience"), List.of("Kubernetes"), 100.0)
    );
  }

  private static Stream<Arguments> fitScoreBoundaries() {
    return Stream.of(
      arguments(named("minimum", 0.0)),
      arguments(named("midpoint", 50.0)),
      arguments(named("maximum", 100.0))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validAnalysis")
  void shouldCreateJobAnalysis(final String summary, final List<String> skills, final double fitScore) {
    // Given, When
    final var analysis = new JobAnalysis(summary, skills, fitScore);

    // Then
    assertThat(analysis.summary()).isEqualTo(summary);
    assertThat(analysis.skills()).containsExactlyElementsOf(skills);
    assertThat(analysis.fitScore()).isEqualTo(fitScore);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fitScoreBoundaries")
  void shouldAcceptAllFitScoreValues(final double fitScore) {
    // Given, When
    final var analysis = new JobAnalysis("test", List.of(), fitScore);

    // Then
    assertThat(analysis.fitScore()).isEqualTo(fitScore);
  }
}
