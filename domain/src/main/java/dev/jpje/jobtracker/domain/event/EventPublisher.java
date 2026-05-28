package dev.jpje.jobtracker.domain.event;

public interface EventPublisher {
  void publish(DomainEvent event);
}
