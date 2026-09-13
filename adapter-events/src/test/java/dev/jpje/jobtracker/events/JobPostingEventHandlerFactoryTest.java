package dev.jpje.jobtracker.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobPostingEventHandlerFactoryTest {

  @Mock
  private JobPostingEventHandler trackingHandler;

  @Mock
  private JobPostingEventHandler analysisHandler;

  private JobPostingEventHandlerFactory factory;

  @BeforeEach
  void setUp() {
    when(trackingHandler.type()).thenReturn(JobPostingEventType.TRACKING);
    when(analysisHandler.type()).thenReturn(JobPostingEventType.ANALYSIS);
    factory = new JobPostingEventHandlerFactory(List.of(trackingHandler, analysisHandler));
  }

  @Test
  void shouldResolveTrackingHandler() {
    assertThat(factory.get(JobPostingEventType.TRACKING)).isSameAs(trackingHandler);
  }

  @Test
  void shouldResolveAnalysisHandler() {
    assertThat(factory.get(JobPostingEventType.ANALYSIS)).isSameAs(analysisHandler);
  }
}
