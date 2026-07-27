package dev.jpje.jobtracker.server;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.application.usecase.AnalyzeJobPostingUseCase;
import dev.jpje.jobtracker.application.usecase.AuthenticationUseCase;
import dev.jpje.jobtracker.application.usecase.GetAnalyticsUseCase;
import dev.jpje.jobtracker.application.usecase.ListJobPostingsUseCase;
import dev.jpje.jobtracker.application.usecase.SubmitJobPostingUseCase;
import dev.jpje.jobtracker.application.usecase.TrackJobApplicationUseCase;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.in.AnalyzeJobPostingPort;
import dev.jpje.jobtracker.domain.port.in.AuthenticationPort;
import dev.jpje.jobtracker.domain.port.in.GetAnalyticsPort;
import dev.jpje.jobtracker.domain.port.in.ListJobPostingsPort;
import dev.jpje.jobtracker.domain.port.in.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.port.in.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.LoadUserPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.SaveUserPort;
import dev.jpje.jobtracker.domain.port.out.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

  @Bean
  SubmitJobPostingPort submitJobPostingUseCase(
      final Clock clock,
      final SaveJobPostingPort savePostingPort,
      final SaveJobApplicationPort saveAppPort,
      final Counter applicationCreatedCounter,
      final Timer submitDurationTimer) {
    final var impl = new SubmitJobPostingUseCase(savePostingPort, saveAppPort, clock);
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
  AnalyzeJobPostingPort analyzeJobPostingUseCase(
      final LoadJobPostingPort loadPort,
      final JobAnalysisPort analysisPort,
      final Timer analyzeJobDurationTimer) {
    final var impl = new AnalyzeJobPostingUseCase(loadPort, analysisPort);
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
  ListJobPostingsPort listJobPostingsUseCase(final LoadJobPostingPort loadPort) {
    return new ListJobPostingsUseCase(loadPort);
  }

  @Bean
  GetAnalyticsPort getAnalyticsUseCase(final LoadJobApplicationPort loadPort) {
    return new GetAnalyticsUseCase(loadPort);
  }

  @Bean
  TrackJobApplicationPort trackJobApplicationUseCase(
      final Clock clock,
      final SaveJobApplicationPort savePort,
      final LoadJobApplicationPort loadPort,
      final Counter applicationCreatedCounter) {
    final var impl = new TrackJobApplicationUseCase(savePort, loadPort, clock);
    return new TrackJobApplicationPort() {
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
  AuthenticationPort authenticationUseCase(final Clock clock,
                                           final SaveUserPort saveUserPort,
                                           final LoadUserPort loadUserPort,
                                           final TokenGeneratorPort tokenGenerator) {
    return new AuthenticationUseCase(saveUserPort, loadUserPort, tokenGenerator, clock);
  }
}
