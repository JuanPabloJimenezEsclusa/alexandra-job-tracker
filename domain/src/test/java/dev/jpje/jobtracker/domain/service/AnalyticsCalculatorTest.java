package dev.jpje.jobtracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.Analytics;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AnalyticsCalculatorTest {

  private static final int MULTIPLE_STATUSES_EXPECTED_TOTAL = 4;
  private static final double MULTIPLE_STATUSES_EXPECTED_CONVERSION = 25.0;
  private static final int EMPTY_LIST_EXPECTED_TOTAL = 0;
  private static final double EMPTY_LIST_EXPECTED_CONVERSION = 0.0;
  private static final int ONLY_SAVED_EXPECTED_TOTAL = 1;
  private static final double ONLY_SAVED_EXPECTED_CONVERSION = 0.0;
  private static final int ALL_ACCEPTED_EXPECTED_TOTAL = 2;
  private static final double ALL_ACCEPTED_EXPECTED_CONVERSION = 100.0;
  private static final int HALF_ACCEPTED_EXPECTED_TOTAL = 2;
  private static final double HALF_ACCEPTED_EXPECTED_CONVERSION = 50.0;

  private static Stream<Arguments> analyticsScenarios() {
    var uid = UserId.generate();
    return Stream.of(
      arguments(named("multiple statuses", List.of(
        app(uid, ApplicationStatus.ACCEPTED),
        app(uid, ApplicationStatus.REJECTED),
        app(uid, ApplicationStatus.INTERVIEWING),
        app(uid, ApplicationStatus.APPLIED))
      ), MULTIPLE_STATUSES_EXPECTED_TOTAL, MULTIPLE_STATUSES_EXPECTED_CONVERSION),
      arguments(named("empty list", List.of()), EMPTY_LIST_EXPECTED_TOTAL, EMPTY_LIST_EXPECTED_CONVERSION),
      arguments(named("only saved", List.of(app(uid, ApplicationStatus.SAVED))), ONLY_SAVED_EXPECTED_TOTAL, ONLY_SAVED_EXPECTED_CONVERSION),
      arguments(named("all accepted", List.of(
        app(uid, ApplicationStatus.ACCEPTED),
        app(uid, ApplicationStatus.ACCEPTED)
      )), ALL_ACCEPTED_EXPECTED_TOTAL, ALL_ACCEPTED_EXPECTED_CONVERSION),
      arguments(named("half accepted", List.of(
        app(uid, ApplicationStatus.ACCEPTED),
        app(uid, ApplicationStatus.OFFER)
      )), HALF_ACCEPTED_EXPECTED_TOTAL, HALF_ACCEPTED_EXPECTED_CONVERSION)
    );
  }

  private static JobApplication app(final UserId userId, final ApplicationStatus status) {
    return Instancio.of(JobApplication.class)
      .set(field(JobApplication::userId), userId)
      .set(field(JobApplication::status), status)
      .set(field(JobApplication::notes), null)
      .create();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("analyticsScenarios")
  void shouldCalculateAnalytics(final List<JobApplication> apps, final int expectedTotal,
                                final double expectedConversion) {
    // Given, when
    final var result = new AnalyticsCalculator().calculate(apps);

    // Then
    assertThat(result)
      .extracting(Analytics::totalApplications, Analytics::conversionRate)
      .containsExactly(expectedTotal, expectedConversion);
  }
}
