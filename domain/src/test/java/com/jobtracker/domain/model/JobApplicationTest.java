package com.jobtracker.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

  static Stream<Arguments> validTransitions() {
    return Stream.of(
      Arguments.of(ApplicationStatus.SAVED,       ApplicationStatus.APPLIED,       "SAVED → APPLIED"),
      Arguments.of(ApplicationStatus.APPLIED,     ApplicationStatus.INTERVIEWING,  "APPLIED → INTERVIEWING"),
      Arguments.of(ApplicationStatus.INTERVIEWING, ApplicationStatus.OFFER,        "INTERVIEWING → OFFER"),
      Arguments.of(ApplicationStatus.OFFER,        ApplicationStatus.ACCEPTED,     "OFFER → ACCEPTED"),
      Arguments.of(ApplicationStatus.OFFER,        ApplicationStatus.REJECTED,     "OFFER → REJECTED"),
      Arguments.of(ApplicationStatus.INTERVIEWING, ApplicationStatus.REJECTED,     "INTERVIEWING → REJECTED"),
      Arguments.of(ApplicationStatus.APPLIED,      ApplicationStatus.REJECTED,     "APPLIED → REJECTED"),
      Arguments.of(ApplicationStatus.SAVED,        ApplicationStatus.WITHDRAWN,    "SAVED → WITHDRAWN")
    );
  }

  static Stream<Arguments> invalidTransitions() {
    return Stream.of(
      Arguments.of(ApplicationStatus.ACCEPTED,     ApplicationStatus.APPLIED,       "ACCEPTED → APPLIED"),
      Arguments.of(ApplicationStatus.REJECTED,      ApplicationStatus.INTERVIEWING,  "REJECTED → INTERVIEWING"),
      Arguments.of(ApplicationStatus.WITHDRAWN,     ApplicationStatus.SAVED,         "WITHDRAWN → SAVED"),
      Arguments.of(ApplicationStatus.INTERVIEWING,  ApplicationStatus.SAVED,         "INTERVIEWING → SAVED")
    );
  }

  private JobApplication app(ApplicationStatus status) {
    return new JobApplication(UUID.randomUUID(), UserId.generate(), "Acme", "SWE",
        Source.LINKEDIN, "https://linkedin.com/jobs/1", status,
        Instant.now(), Instant.now(), null);
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("validTransitions")
  void shouldTransitionToValidStatus(ApplicationStatus from, ApplicationStatus to, String _name) {
    // Given
    var app = app(from);

    // When
    var updated = app.withStatus(to);

    // Then
    assertThat(updated.status()).isEqualTo(to);
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("invalidTransitions")
  void shouldNotTransitionToInvalidStatus(ApplicationStatus from, ApplicationStatus to, String _name) {
    // Given
    var app = app(from);

    // When / Then
    assertThatThrownBy(() -> app.withStatus(to))
        .isInstanceOf(IllegalStateException.class);
  }
}
