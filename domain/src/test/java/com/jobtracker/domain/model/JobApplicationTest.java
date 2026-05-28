package com.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JobApplicationTest {

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

  @ParameterizedTest(name = "{0}")
  @MethodSource("validTransitions")
  void shouldTransitionToValidStatus(final ApplicationStatus from, final ApplicationStatus to) {
    // Given
    final var app = new JobApplication(UUID.randomUUID(), UserId.generate(), "Acme", "SWE",
      Source.LINKEDIN, "https://linkedin.com/jobs/1", from,
      Instant.now(), Instant.now(), null);

    // When
    final var updated = app.withStatus(to);

    // Then
    assertThat(updated.status()).isEqualTo(to);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidTransitions")
  void shouldNotTransitionToInvalidStatus(final ApplicationStatus from, final ApplicationStatus to) {
    // Given
    final var app = new JobApplication(UUID.randomUUID(), UserId.generate(), "Acme", "SWE",
      Source.LINKEDIN, "https://linkedin.com/jobs/1", from,
      Instant.now(), Instant.now(), null);

    // When, Then
    assertThatThrownBy(() -> app.withStatus(to))
      .isInstanceOf(IllegalStateException.class);
  }
}
