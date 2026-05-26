package com.jobtracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnalyticsCalculatorTest {

  private static Stream<Arguments> analyticsScenarios() {
    var uid = UserId.generate();
    return Stream.of(
      arguments(named("multiple statuses", List.of(
          app(uid, ApplicationStatus.ACCEPTED),
          app(uid, ApplicationStatus.REJECTED),
          app(uid, ApplicationStatus.INTERVIEWING),
          app(uid, ApplicationStatus.APPLIED))
        ), 4, 25.0),
      arguments(named("empty list", List.of()), 0, 0.0),
      arguments(named("only saved", List.of(app(uid, ApplicationStatus.SAVED))), 1, 0.0),
      arguments(named("all accepted", List.of(
          app(uid, ApplicationStatus.ACCEPTED),
          app(uid, ApplicationStatus.ACCEPTED)
        )), 2, 100.0),
      arguments(named("half accepted", List.of(
          app(uid, ApplicationStatus.ACCEPTED),
          app(uid, ApplicationStatus.OFFER)
        )), 2, 50.0)
    );
  }

  private static JobApplication app(final UserId userId, final ApplicationStatus status) {
    return new JobApplication(UUID.randomUUID(), userId, "Acme", "SWE",
      Source.LINKEDIN, null, status, Instant.now(), Instant.now(), null);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("analyticsScenarios")
  void shouldCalculateAnalytics(final List<JobApplication> apps, final int expectedTotal,
                                final double expectedConversion) {
    // Given, When
    var result = new AnalyticsCalculator().calculate(apps);

    // Then
    assertThat(result.totalApplications()).isEqualTo(expectedTotal);
    if (expectedConversion == 0.0) {
      assertThat(result.conversionRate()).isZero();
    } else {
      assertThat(result.conversionRate()).isCloseTo(expectedConversion, withinPercentage(0.1));
    }
  }
}
