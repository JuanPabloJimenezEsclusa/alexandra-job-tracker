package dev.jpje.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.exception.InvalidStateTransitionException;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobApplicationTest {

  private static final long INITIAL_VERSION = 0L;

  private static Stream<Arguments> validTransitions() {
    return Stream.of(
      arguments(named("SAVED → APPLIED", ApplicationStatus.SAVED), ApplicationStatus.APPLIED),
      arguments(named("APPLIED → INTERVIEWING", ApplicationStatus.APPLIED), ApplicationStatus.INTERVIEWING),
      arguments(named("INTERVIEWING → OFFER", ApplicationStatus.INTERVIEWING), ApplicationStatus.OFFER),
      arguments(named("OFFER → ACCEPTED", ApplicationStatus.OFFER), ApplicationStatus.ACCEPTED),
      arguments(named("OFFER → REJECTED", ApplicationStatus.OFFER), ApplicationStatus.REJECTED),
      arguments(named("INTERVIEWING → REJECTED", ApplicationStatus.INTERVIEWING), ApplicationStatus.REJECTED),
      arguments(named("APPLIED → REJECTED", ApplicationStatus.APPLIED), ApplicationStatus.REJECTED),
      arguments(named("SAVED → WITHDRAWN", ApplicationStatus.SAVED), ApplicationStatus.WITHDRAWN)
    );
  }

  private static Stream<Arguments> invalidTransitions() {
    return Stream.of(
      arguments(named("ACCEPTED → APPLIED", ApplicationStatus.ACCEPTED), ApplicationStatus.APPLIED),
      arguments(named("REJECTED → INTERVIEWING", ApplicationStatus.REJECTED), ApplicationStatus.INTERVIEWING),
      arguments(named("WITHDRAWN → SAVED", ApplicationStatus.WITHDRAWN), ApplicationStatus.SAVED),
      arguments(named("INTERVIEWING → SAVED", ApplicationStatus.INTERVIEWING), ApplicationStatus.SAVED)
    );
  }

  private static Stream<Arguments> invalidInputs() {
    final var uid = UserId.generate();
    final var postingId = UUID.randomUUID();
    final var now = Instant.EPOCH;
    return Stream.of(
      arguments(named("null id", ""), null, uid, postingId, ApplicationStatus.SAVED, now, now, null),
      arguments(named("null userId", ""), UUID.randomUUID(), null, postingId, ApplicationStatus.SAVED, now, now, null),
      arguments(named("null jobPostingId", ""), UUID.randomUUID(), uid, null, ApplicationStatus.SAVED, now, now, null),
      arguments(named("null status", ""), UUID.randomUUID(), uid, postingId, null, now, now, null),
      arguments(named("null dateApplied", ""), UUID.randomUUID(), uid, postingId, ApplicationStatus.SAVED, null, now, null),
      arguments(named("null lastUpdated", ""), UUID.randomUUID(), uid, postingId, ApplicationStatus.SAVED, now, null, null)
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("validTransitions")
  void shouldTransitionToValidStatus(final ApplicationStatus from, final ApplicationStatus to) {
    // Given
    final var now = Instant.EPOCH;
    final var app = new JobApplication(UUID.randomUUID(), UserId.generate(), UUID.randomUUID(), from,
      now, now, null, INITIAL_VERSION);

    // When, then
    assertThat(app.withStatus(to, now))
      .returns(to, JobApplication::status)
      .returns(INITIAL_VERSION, JobApplication::version);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidTransitions")
  void shouldNotTransitionToInvalidStatus(final ApplicationStatus from, final ApplicationStatus to) {
    // Given
    final var now = Instant.EPOCH;
    final var app = new JobApplication(UUID.randomUUID(), UserId.generate(), UUID.randomUUID(), from,
      now, now, null, INITIAL_VERSION);

    // When, then
    assertThatThrownBy(() -> app.withStatus(to, now))
      .isInstanceOf(InvalidStateTransitionException.class);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidInputs")
  void shouldRejectInvalidInputs(final String unused, final UUID id, final UserId userId,
                                  final UUID jobPostingId, final ApplicationStatus status,
                                  final Instant dateApplied, final Instant lastUpdated,
                                  final Notes notes) {
    assertThatThrownBy(() -> new JobApplication(id, userId, jobPostingId,
      status, dateApplied, lastUpdated, notes, INITIAL_VERSION))
      .isInstanceOf(RuntimeException.class);
  }
}
