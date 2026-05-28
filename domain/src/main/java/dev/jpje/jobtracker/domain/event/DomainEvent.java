package dev.jpje.jobtracker.domain.event;

import java.time.Instant;

public interface DomainEvent {
  Instant occurredAt();
}
