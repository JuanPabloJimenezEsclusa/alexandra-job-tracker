package dev.jpje.jobtracker.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.EnumMap;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnalyticsTest {

  private static final int ONLY_SAVED_EXPECTED_TOTAL = 2;
  private static final int MIXED_EXPECTED_TOTAL = 5;
  private static final int ALL_ZERO_EXPECTED_TOTAL = 0;

  private static Stream<Arguments> validAnalytics() {
    final var onlySaved = new EnumMap<ApplicationStatus, Integer>(ApplicationStatus.class);
    onlySaved.put(ApplicationStatus.SAVED, 2);

    final var mixed = new EnumMap<ApplicationStatus, Integer>(ApplicationStatus.class);
    mixed.put(ApplicationStatus.SAVED, 2);
    mixed.put(ApplicationStatus.APPLIED, 3);

    final var allZero = new EnumMap<ApplicationStatus, Integer>(ApplicationStatus.class);
    return Stream.of(
      arguments(named("only saved", onlySaved), ONLY_SAVED_EXPECTED_TOTAL),
      arguments(named("mixed statuses", mixed), MIXED_EXPECTED_TOTAL),
      arguments(named("all zero", allZero), ALL_ZERO_EXPECTED_TOTAL)
    );
  }

  private static Stream<Arguments> invalidAnalytics() {
    final var negative = new EnumMap<ApplicationStatus, Integer>(ApplicationStatus.class);
    negative.put(ApplicationStatus.APPLIED, -1);
    return Stream.of(
      arguments(named("negative count", negative))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validAnalytics")
  void shouldComputeTotalFromMap(final EnumMap<ApplicationStatus, Integer> perStatus, final int expectedTotal) {
    // When, then
    assertThat(new Analytics(perStatus))
      .as("per-status counts").returns(perStatus, Analytics::perStatus)
      .as("total applications").returns(expectedTotal, Analytics::totalApplications);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidAnalytics")
  void shouldRejectNegativeCounts(final EnumMap<ApplicationStatus, Integer> perStatus) {
    // When, then
    assertThatThrownBy(() -> new Analytics(perStatus))
      .isInstanceOf(IllegalArgumentException.class);
  }
}
