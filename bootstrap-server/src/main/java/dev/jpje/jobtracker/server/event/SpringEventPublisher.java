package dev.jpje.jobtracker.server.event;

import dev.jpje.jobtracker.domain.event.DomainEvent;
import dev.jpje.jobtracker.domain.event.EventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisher implements EventPublisher {
  private final ApplicationEventPublisher springPublisher;

  public SpringEventPublisher(final ApplicationEventPublisher springPublisher) {
    this.springPublisher = springPublisher;
  }

  @Override
  public void publish(final DomainEvent event) {
    springPublisher.publishEvent(event);
  }
}
