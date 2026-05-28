package dev.jpje.jobtracker.server.event;

import dev.jpje.jobtracker.domain.event.DomainEvent;
import dev.jpje.jobtracker.domain.event.EventPublisher;
import io.awspring.cloud.sns.core.SnsNotification;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class DelegatingEventPublisher implements EventPublisher {
  private static final Logger log = LoggerFactory.getLogger(DelegatingEventPublisher.class);

  private final ApplicationEventPublisher springPublisher;
  private final ObjectProvider<SnsTemplate> snsTemplateProvider;
  private final String transport;
  private final String topicArn;

  public DelegatingEventPublisher(final ApplicationEventPublisher springPublisher,
                                  final ObjectProvider<SnsTemplate> snsTemplateProvider,
                                  @Value("${ajt.events.transport:spring}") final String transport,
                                  @Value("${ajt.events.sns-topic:}") final String topicArn) {
    this.springPublisher = springPublisher;
    this.snsTemplateProvider = snsTemplateProvider;
    this.transport = transport;
    this.topicArn = topicArn;
  }

  @Override
  public void publish(final DomainEvent event) {
    if ("sns".equals(transport) && !topicArn.isBlank()) {
      final var snsTemplate = snsTemplateProvider.getIfAvailable();
      if (snsTemplate != null) {
        log.debug("Publishing domain event {} to SNS topic {}", event, topicArn);
        snsTemplate.sendNotification(topicArn, SnsNotification.builder(event)
          .subject(event.getClass().getSimpleName())
          .header("eventType", event.getClass().getSimpleName())
          .build());
        return;
      }
    }
    log.debug("Publishing domain event {}", event);
    springPublisher.publishEvent(event);
  }
}
