package dev.jpje.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

  private static Stream<Arguments> invalidInputs() {
    return Stream.of(
      arguments(named("null summary", ""), null, List.of("Java"), 50.0),
      arguments(named("null skills", ""), "summary", null, 50.0),
      arguments(named("fitScore too low", ""), "summary", List.of(), -0.1),
      arguments(named("fitScore too high", ""), "summary", List.of(), 100.1)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validAnalysis")
  void shouldCreateJobAnalysis(final String summary, final List<String> skills, final double fitScore) {
    // Given, when, then
    assertThat(new JobAnalysis(summary, skills, fitScore))
      .returns(summary, JobAnalysis::summary)
      .returns(skills, JobAnalysis::skills)
      .returns(fitScore, JobAnalysis::fitScore);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("fitScoreBoundaries")
  void shouldAcceptAllFitScoreValues(final double fitScore) {
    // Given, when, then
    assertThat(new JobAnalysis("test", List.of(), fitScore))
      .returns(fitScore, JobAnalysis::fitScore);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidInputs")
  void shouldRejectInvalidInputs(final String unused, final String summary,
                                  final List<String> skills, final double fitScore) {
    assertThatThrownBy(() -> new JobAnalysis(summary, skills, fitScore))
      .isInstanceOf(RuntimeException.class);
  }
}
