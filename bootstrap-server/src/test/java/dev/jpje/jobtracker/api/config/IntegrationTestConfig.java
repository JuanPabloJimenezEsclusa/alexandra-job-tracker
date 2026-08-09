package dev.jpje.jobtracker.api.config;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import org.instancio.Instancio;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class IntegrationTestConfig {

  @Bean
  @Primary
  JobAnalysisPort jobAnalysisPort() {
    final var mock = mock(JobAnalysisPort.class);
    final var jobAnalysis = Instancio.of(JobAnalysis.class)
      .set(field(JobAnalysis::summary), "Mocked analysis")
      .set(field(JobAnalysis::fitScore), 85.0)
      .set(field(JobAnalysis::companyRating), 4.2)
      .set(field(JobAnalysis::companyType), "enterprise")
      .set(field(JobAnalysis::salaryMin), 90000.0)
      .set(field(JobAnalysis::salaryMax), 130000.0)
      .set(field(JobAnalysis::salaryCurrency), "USD")
      .create();
    when(mock.analyze(anyString())).thenReturn(jobAnalysis);
    return mock;
  }
}
