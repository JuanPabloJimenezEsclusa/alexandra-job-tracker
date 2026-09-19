package dev.jpje.jobtracker.application.port.outbound;

import dev.jpje.jobtracker.domain.event.DomainEvent;

public interface EventPublisher {
  void publish(DomainEvent event);
}
