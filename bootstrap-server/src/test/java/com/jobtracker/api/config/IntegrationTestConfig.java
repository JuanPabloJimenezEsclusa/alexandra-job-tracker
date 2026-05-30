package com.jobtracker.api.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jobtracker.domain.port.out.JobAnalysisPort;
import com.jobtracker.domain.port.out.JobScraperPort;
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
  JobScraperPort jobScraperPort() {
    final var mock = mock(JobScraperPort.class);
    when(mock.scrape(any(), anyString())).thenAnswer(invocation -> {
      final var url = invocation.getArgument(1, String.class);
      return new com.jobtracker.domain.port.out.RawJobData(url, "Mocked Engineer",
        "Acme Corp", "Job description", "LINKEDIN");
    });
    return mock;
  }

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
