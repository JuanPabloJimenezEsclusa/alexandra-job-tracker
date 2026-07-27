package dev.jpje.jobtracker.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Source;
import org.instancio.Instancio;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ApplicationTrackerTest {

  private final ApplicationTracker service = new ApplicationTracker();

  private static Stream<Arguments> validTransitions() {
    return Stream.of(
      arguments(named("SAVED → APPLIED", ApplicationStatus.SAVED), ApplicationStatus.APPLIED),
      arguments(named("APPLIED → INTERVIEWING", ApplicationStatus.APPLIED), ApplicationStatus.INTERVIEWING)
    );
  }

  private static Stream<Arguments> invalidTransitions() {
    return Stream.of(
      arguments(named("ACCEPTED → APPLIED", ApplicationStatus.ACCEPTED), ApplicationStatus.APPLIED),
      arguments(named("REJECTED → SAVED", ApplicationStatus.REJECTED), ApplicationStatus.SAVED)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validTransitions")
  void shouldTransitionStatus(final ApplicationStatus from, final ApplicationStatus to) {
    // Given
    final var app = Instancio.of(JobApplication.class)
      .set(field(JobApplication::source), Source.LINKEDIN)
      .set(field(JobApplication::status), from)
      .set(field(JobApplication::postingUrl), null)
      .set(field(JobApplication::notes), null)
      .create();

    // When
    assertThat(service.transitionStatus(app, to, Instant.EPOCH))
      .returns(app.id(), JobApplication::id)
      .returns(app.userId(), JobApplication::userId)
      .returns(app.company(), JobApplication::company)
      .returns(app.source(), JobApplication::source)
      .returns(app.postingUrl(), JobApplication::postingUrl)
      .returns(to, JobApplication::status)
      .returns(app.dateApplied(), JobApplication::dateApplied);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidTransitions")
  void shouldRejectInvalidTransition(final ApplicationStatus from, final ApplicationStatus to) {
    // Given
    final var app = Instancio.of(JobApplication.class)
      .set(field(JobApplication::source), Source.LINKEDIN)
      .set(field(JobApplication::status), from)
      .set(field(JobApplication::postingUrl), null)
      .set(field(JobApplication::notes), null)
      .create();

    // When, then
    assertThatThrownBy(() -> service.transitionStatus(app, to, Instant.EPOCH))
      .isInstanceOf(IllegalStateException.class);
  }
}
