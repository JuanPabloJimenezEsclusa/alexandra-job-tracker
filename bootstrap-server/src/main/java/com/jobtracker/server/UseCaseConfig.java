package com.jobtracker.server;

import java.time.Clock;

import com.jobtracker.application.service.AnalyzeJobPostingUseCaseImpl;
import com.jobtracker.application.service.AuthenticationUseCaseImpl;
import com.jobtracker.application.service.GetAnalyticsUseCaseImpl;
import com.jobtracker.application.service.ListJobPostingsUseCaseImpl;
import com.jobtracker.application.service.SubmitJobPostingUseCaseImpl;
import com.jobtracker.application.service.TrackJobApplicationUseCaseImpl;
import com.jobtracker.domain.port.in.AnalyzeJobPostingUseCase;
import com.jobtracker.domain.port.in.AuthenticationUseCase;
import com.jobtracker.domain.port.in.GetAnalyticsUseCase;
import com.jobtracker.domain.port.in.ListJobPostingsUseCase;
import com.jobtracker.domain.port.in.SubmitJobPostingUseCase;
import com.jobtracker.domain.port.in.TrackJobApplicationUseCase;
import com.jobtracker.domain.port.out.JobAnalysisPort;
import com.jobtracker.domain.port.out.LoadJobApplicationPort;
import com.jobtracker.domain.port.out.LoadJobPostingPort;
import com.jobtracker.domain.port.out.LoadUserPort;
import com.jobtracker.domain.port.out.SaveJobApplicationPort;
import com.jobtracker.domain.port.out.SaveJobPostingPort;
import com.jobtracker.domain.port.out.SaveUserPort;
import com.jobtracker.domain.port.out.TokenGeneratorPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires application use case implementations into Spring-managed beans.
 */
@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

  @Bean
  SubmitJobPostingUseCase submitJobPostingUseCase(
      final Clock clock,
      final SaveJobPostingPort savePostingPort,
      final SaveJobApplicationPort saveAppPort) {
    return new SubmitJobPostingUseCaseImpl(savePostingPort, saveAppPort, clock);
  }

  @Bean
  AnalyzeJobPostingUseCase analyzeJobPostingUseCase(final LoadJobPostingPort loadPort,
                                                    final JobAnalysisPort analysisPort) {
    return new AnalyzeJobPostingUseCaseImpl(loadPort, analysisPort);
  }

  @Bean
  ListJobPostingsUseCase listJobPostingsUseCase(final LoadJobPostingPort loadPort) {
    return new ListJobPostingsUseCaseImpl(loadPort);
  }

  @Bean
  GetAnalyticsUseCase getAnalyticsUseCase(final LoadJobApplicationPort loadPort) {
    return new GetAnalyticsUseCaseImpl(loadPort);
  }

  @Bean
  TrackJobApplicationUseCase trackJobApplicationUseCase(
      final Clock clock,
      final SaveJobApplicationPort savePort,
      final LoadJobApplicationPort loadPort) {
    return new TrackJobApplicationUseCaseImpl(savePort, loadPort, clock);
  }

  @Bean
  AuthenticationUseCase authenticationUseCase(final Clock clock,
                                              final SaveUserPort saveUserPort,
                                              final LoadUserPort loadUserPort,
                                              final TokenGeneratorPort tokenGenerator) {
    return new AuthenticationUseCaseImpl(saveUserPort, loadUserPort, tokenGenerator, clock);
  }
}
