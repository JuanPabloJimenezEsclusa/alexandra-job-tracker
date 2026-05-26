package com.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobAnalysisTest {

  static Stream<Arguments> validAnalysis() {
    return Stream.of(
      Arguments.of("Great role", List.of("Java", "Spring"), 85.0, "full analysis"),
      Arguments.of("", List.of(), 0.0, "empty analysis"),
      Arguments.of("Needs experience", List.of("Kubernetes"), 100.0, "single skill")
    );
  }

  @ParameterizedTest(name = "{3}")
  @MethodSource("validAnalysis")
  void shouldCreateJobAnalysis(String summary, List<String> skills, double fitScore, String _name) {
    // Given / When
    var analysis = new JobAnalysis(summary, skills, fitScore);

    // Then
    assertThat(analysis.summary()).isEqualTo(summary);
    assertThat(analysis.skills()).containsExactlyElementsOf(skills);
    assertThat(analysis.fitScore()).isEqualTo(fitScore);
  }

  static Stream<Arguments> fitScoreBoundaries() {
    return Stream.of(
      Arguments.of(0.0,   "minimum"),
      Arguments.of(50.0,  "midpoint"),
      Arguments.of(100.0, "maximum")
    );
  }

  @ParameterizedTest(name = "fitScore={0} ({1})")
  @MethodSource("fitScoreBoundaries")
  void shouldAcceptAllFitScoreValues(double fitScore, String _name) {
    // Given / When
    var analysis = new JobAnalysis("test", List.of(), fitScore);

    // Then
    assertThat(analysis.fitScore()).isEqualTo(fitScore);
  }
}
