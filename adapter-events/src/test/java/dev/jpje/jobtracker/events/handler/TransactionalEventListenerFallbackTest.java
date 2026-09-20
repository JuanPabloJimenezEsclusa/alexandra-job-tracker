package dev.jpje.jobtracker.events.handler;

import static org.mockito.Mockito.description;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringJUnitConfig(TransactionalEventListenerFallbackTest.ListenerConfig.class)
class TransactionalEventListenerFallbackTest {

  private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"),
    ZoneOffset.UTC);

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Autowired
  private JobPostingEventHandlerFactory handlerFactory;

  @Test
  void shouldDispatchWithoutActiveTransaction() {
    // Given
    final var trackingHandler = mock(JobPostingEventHandler.class);
    final var analysisHandler = mock(JobPostingEventHandler.class);
    when(handlerFactory.get(JobPostingEventType.TRACKING)).thenReturn(trackingHandler);
    when(handlerFactory.get(JobPostingEventType.ANALYSIS)).thenReturn(analysisHandler);
    final var event = event();

    // When
    eventPublisher.publishEvent(event);

    // Then
    verify(trackingHandler, description("tracking handler invoked without an active transaction")).handle(event);
    verify(analysisHandler, description("analysis handler invoked without an active transaction")).handle(event);
    verifyNoMoreInteractions(trackingHandler, analysisHandler);
  }

  private static JobPostingCreated event() {
    final var posting = new JobPosting(
      UUID.randomUUID(),
      UserId.generate(),
      Url.of("https://example.com/job"),
      Source.LINKEDIN,
      JobTitle.of("Engineer"),
      CompanyName.of("Acme"),
      "Java developer with Spring experience",
      FIXED_CLOCK.instant());
    return JobPostingCreated.of(posting, FIXED_CLOCK.instant());
  }

  @Configuration(proxyBeanMethods = false)
  @EnableTransactionManagement
  static class ListenerConfig {

    @Bean
    JobPostingEventHandlerFactory handlerFactory() {
      return mock(JobPostingEventHandlerFactory.class);
    }

    @Bean
    JobPostingEventListeners jobPostingEventListeners(final JobPostingEventHandlerFactory handlerFactory) {
      return new JobPostingEventListeners(handlerFactory);
    }
  }
}
