package dev.jpje.jobtracker.domain.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;

class JobApplicationStatusChangedTest {

  @Test
  void shouldExposeEventDetails() {
    final var id = UUID.randomUUID();
    final var userId = UserId.generate();
    final var occurredAt = Instant.EPOCH;

    assertThat(new JobApplicationStatusChanged(
      id, userId, ApplicationStatus.APPLIED, ApplicationStatus.REJECTED, occurredAt))
      .extracting(
        JobApplicationStatusChanged::applicationId,
        JobApplicationStatusChanged::userId,
        JobApplicationStatusChanged::previousStatus,
        JobApplicationStatusChanged::newStatus,
        JobApplicationStatusChanged::occurredAt)
      .containsExactly(id, userId, ApplicationStatus.APPLIED, ApplicationStatus.REJECTED, occurredAt);
  }
}
