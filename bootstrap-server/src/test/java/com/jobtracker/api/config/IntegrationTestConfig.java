package com.jobtracker.api.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jobtracker.domain.port.out.JobAnalysisPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * The type Integration test config.
 */
@TestConfiguration
public class IntegrationTestConfig {

  @Bean
  @Primary
  JobAnalysisPort jobAnalysisPort() {
    final var mock = mock(JobAnalysisPort.class);
    when(mock.analyze(anyString())).thenReturn(
      new com.jobtracker.domain.model.JobAnalysis("Mocked analysis",
        java.util.List.of("Java", "Spring"), 75.0));
    return mock;
  }
}
