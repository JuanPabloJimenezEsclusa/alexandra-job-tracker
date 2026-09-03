package dev.jpje.jobtracker.server;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.application.usecase.AnalyzeJobPostingUseCase;
import dev.jpje.jobtracker.application.usecase.AuthenticationUseCase;
import dev.jpje.jobtracker.application.usecase.GetAnalyticsUseCase;
import dev.jpje.jobtracker.application.usecase.ListJobPostingsUseCase;
import dev.jpje.jobtracker.application.usecase.ManageJobAnalysisUseCase;
import dev.jpje.jobtracker.application.usecase.SubmitJobPostingUseCase;
import dev.jpje.jobtracker.application.usecase.TrackJobApplicationUseCase;
import dev.jpje.jobtracker.domain.event.EventPublisher;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.in.AnalyzeJobPostingPort;
import dev.jpje.jobtracker.domain.port.in.AuthenticationPort;
import dev.jpje.jobtracker.domain.port.in.GetAnalyticsPort;
import dev.jpje.jobtracker.domain.port.in.ListJobPostingsPort;
import dev.jpje.jobtracker.domain.port.in.ManageJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.in.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.port.in.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.LoadUserPort;
import dev.jpje.jobtracker.domain.port.out.PasswordEncoderPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.SaveUserPort;
import dev.jpje.jobtracker.domain.port.out.TokenGeneratorPort;
import dev.jpje.jobtracker.domain.service.JobPostingService;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration(proxyBeanMethods = false)
public class UseCaseConfig {

  @Bean
  TransactionTemplate transactionTemplate(final PlatformTransactionManager transactionManager) {
    return new TransactionTemplate(transactionManager);
  }

  @Bean
  JobPostingService jobPostingService(final EventPublisher eventPublisher, final Clock clock) {
    return new JobPostingService(eventPublisher, clock);
  }

  @Bean
  SubmitJobPostingPort submitJobPostingUseCase(
      final Clock clock,
      final SaveJobPostingPort savePostingPort,
      final JobPostingService jobPostingService,
      final Timer submitDurationTimer,
      final TransactionTemplate transactionTemplate) {
    final var impl = new SubmitJobPostingUseCase(savePostingPort, jobPostingService, clock);
    return (userId, url, title, company, description, source) -> {
      final var sample = Timer.start();
      try {
        return transactionTemplate.execute(_ -> impl.submit(userId, url, title, company, description, source));
      } finally {
        sample.stop(submitDurationTimer);
      }
    };
  }

  @Bean
  AnalyzeJobPostingPort analyzeJobPostingUseCase(
      final Clock clock,
      final LoadJobPostingPort loadPort,
      final JobAnalysisPort analysisPort,
      final SaveJobAnalysisPort saveAnalysisPort,
      final Timer analyzeJobDurationTimer) {
    final var impl = new AnalyzeJobPostingUseCase(loadPort, analysisPort, saveAnalysisPort, clock);
    return (userId, jobPostingId) -> {
      final var sample = Timer.start();
      try {
        return impl.analyze(userId, jobPostingId);
      } finally {
        sample.stop(analyzeJobDurationTimer);
      }
    };
  }

  @Bean
  ManageJobAnalysisPort manageJobAnalysisUseCase(
      final LoadJobAnalysisPort loadAnalysisPort,
      final SaveJobAnalysisPort saveAnalysisPort) {
    return new ManageJobAnalysisUseCase(loadAnalysisPort, saveAnalysisPort);
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
      final LoadJobPostingPort loadPostingPort,
      final EventPublisher eventPublisher,
      final Counter applicationCreatedCounter) {
    final var impl = new TrackJobApplicationUseCase(savePort, loadPort, loadPostingPort, clock, eventPublisher);
    return new TrackJobApplicationPort() {
      @Override
      public JobApplication create(final UserId userId, final UUID jobPostingId,
                                    @Nullable final Notes notes) {
        final var result = impl.create(userId, jobPostingId, notes);
        applicationCreatedCounter.increment();
        return result;
      }

      @Override
      public JobApplication updateStatus(final UserId userId, final UUID applicationId,
                                          final ApplicationStatus newStatus,
                                          @Nullable final Notes notes) {
        return impl.updateStatus(userId, applicationId, newStatus, notes);
      }

      @Override
      public List<JobApplication> list(final UserId userId, @Nullable final ApplicationStatus status) {
        return impl.list(userId, status);
      }

      @Override
      public void delete(final UserId userId, final UUID applicationId) {
        impl.delete(userId, applicationId);
      }
    };
  }

  @Bean
  AuthenticationPort authenticationUseCase(final Clock clock,
                                           final SaveUserPort saveUserPort,
                                           final LoadUserPort loadUserPort,
                                           final TokenGeneratorPort tokenGenerator,
                                           final PasswordEncoderPort passwordEncoder,
                                           final EventPublisher eventPublisher) {
    return new AuthenticationUseCase(saveUserPort, loadUserPort, tokenGenerator, passwordEncoder, clock,
      eventPublisher);
  }
}
