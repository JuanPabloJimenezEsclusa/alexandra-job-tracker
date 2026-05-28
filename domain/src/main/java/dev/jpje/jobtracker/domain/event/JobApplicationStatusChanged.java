package dev.jpje.jobtracker.domain.event;

import java.time.Instant;
import java.util.UUID;

import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;

public record JobApplicationStatusChanged(
    UUID applicationId, UserId userId, ApplicationStatus previousStatus,
    ApplicationStatus newStatus, Instant occurredAt) implements DomainEvent {
}
