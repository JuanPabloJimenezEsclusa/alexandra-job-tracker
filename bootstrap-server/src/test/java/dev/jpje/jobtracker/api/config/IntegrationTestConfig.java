package dev.jpje.jobtracker.api.config;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import dev.jpje.jobtracker.domain.model.JobAnalysis;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class IntegrationTestConfig {

  @Bean
  @Primary
  JobAnalysisPort jobAnalysisPort() {
    final var mock = mock(JobAnalysisPort.class);
    when(mock.analyze(anyString())).thenReturn(
      new JobAnalysis("Mocked analysis", List.of("Java", "Spring"), 75.0));
    return mock;
  }
}
