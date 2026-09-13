package dev.jpje.jobtracker.events;

import dev.jpje.jobtracker.domain.event.DomainEvent;
import dev.jpje.jobtracker.domain.event.EventPublisher;
import io.awspring.cloud.sns.core.SnsNotification;
import io.awspring.cloud.sns.core.SnsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("aws")
public class SnsEventPublisher implements EventPublisher {
  private static final Logger log = LoggerFactory.getLogger(SnsEventPublisher.class);

  private final SnsTemplate snsTemplate;
  private final String topicArn;

  public SnsEventPublisher(final SnsTemplate snsTemplate,
                           @Value("${ajt.events.sns-topic:}") final String topicArn) {
    this.snsTemplate = snsTemplate;
    this.topicArn = topicArn;
  }

  @Override
  public void publish(final DomainEvent event) {
    final var eventType = event.getClass().getSimpleName();
    final var notification = SnsNotification.builder(event)
      .subject(eventType)
      .header("eventType", eventType)
      .build();
    log.debug("Publishing event {} to SNS topic {}", eventType, topicArn);
    snsTemplate.sendNotification(topicArn, notification);
  }
}
