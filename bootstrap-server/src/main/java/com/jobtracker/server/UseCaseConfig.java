package com.jobtracker.server;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import com.jobtracker.application.service.AnalyzeJobPostingUseCaseImpl;
import com.jobtracker.application.service.AuthenticationUseCaseImpl;
import com.jobtracker.application.service.GetAnalyticsUseCaseImpl;
import com.jobtracker.application.service.ListJobPostingsUseCaseImpl;
import com.jobtracker.application.service.SubmitJobPostingUseCaseImpl;
import com.jobtracker.application.service.TrackJobApplicationUseCaseImpl;
import com.jobtracker.domain.model.JobApplication;
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
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.jspecify.annotations.Nullable;
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
      final SaveJobApplicationPort saveAppPort,
      final Counter applicationCreatedCounter,
      final Timer submitDurationTimer) {
    final var impl = new SubmitJobPostingUseCaseImpl(savePostingPort, saveAppPort, clock);
    return (userId, url, title, company, description, source) -> {
      final var sample = Timer.start();
      try {
        final var result = impl.submit(userId, url, title, company, description, source);
        applicationCreatedCounter.increment();
        return result;
      } finally {
        sample.stop(submitDurationTimer);
      }
    };
  }

  @Bean
  AnalyzeJobPostingUseCase analyzeJobPostingUseCase(
      final LoadJobPostingPort loadPort,
      final JobAnalysisPort analysisPort,
      final Timer analyzeJobDurationTimer) {
    final var impl = new AnalyzeJobPostingUseCaseImpl(loadPort, analysisPort);
    return jobPostingId -> {
      final var sample = Timer.start();
      try {
        return impl.analyze(jobPostingId);
      } finally {
        sample.stop(analyzeJobDurationTimer);
      }
    };
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
      final LoadJobApplicationPort loadPort,
      final Counter applicationCreatedCounter) {
    final var impl = new TrackJobApplicationUseCaseImpl(savePort, loadPort, clock);
    return new TrackJobApplicationUseCase() {
      @Override
      public JobApplication create(final UserId userId, final String company, final String role,
                                   final Source source, @Nullable final String postingUrl,
                                   @Nullable final String notes) {
        final var result = impl.create(userId, company, role, source, postingUrl, notes);
        applicationCreatedCounter.increment();
        return result;
      }

      @Override
      public JobApplication updateStatus(final UUID applicationId, final ApplicationStatus newStatus,
                                         @Nullable final String notes) {
        return impl.updateStatus(applicationId, newStatus, notes);
      }

      @Override
      public List<JobApplication> list(final UserId userId, @Nullable final ApplicationStatus status,
                                       @Nullable final Source source) {
        return impl.list(userId, status, source);
      }

      @Override
      public void delete(final UUID applicationId) {
        impl.delete(applicationId);
      }
    };
  }

  @Bean
  AuthenticationUseCase authenticationUseCase(final Clock clock,
                                              final SaveUserPort saveUserPort,
                                              final LoadUserPort loadUserPort,
                                              final TokenGeneratorPort tokenGenerator) {
    return new AuthenticationUseCaseImpl(saveUserPort, loadUserPort, tokenGenerator, clock);
  }
}
