package dev.jpje.jobtracker.domain.event;

import java.time.Instant;
import java.util.UUID;

import dev.jpje.jobtracker.domain.vo.UserId;

public record JobPostingSubmitted(UUID jobPostingId, UserId userId, Instant occurredAt) implements DomainEvent {
}
