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
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.RoleName;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
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
    return Instancio.of(JobApplication.class)
      .set(field(JobApplication::userId), userId)
      .set(field(JobApplication::status), status)
      .set(field(JobApplication::company), CompanyName.of("Acme"))
      .set(field(JobApplication::role), RoleName.of("SWE"))
      .set(field(JobApplication::source), Source.LINKEDIN)
      .set(field(JobApplication::postingUrl), null)
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
