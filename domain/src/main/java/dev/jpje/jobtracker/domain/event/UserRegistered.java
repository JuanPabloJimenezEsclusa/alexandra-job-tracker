package dev.jpje.jobtracker.domain.event;

import java.time.Instant;

import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.Username;

public record UserRegistered(UserId userId, Username username, Instant occurredAt) implements DomainEvent {
}
