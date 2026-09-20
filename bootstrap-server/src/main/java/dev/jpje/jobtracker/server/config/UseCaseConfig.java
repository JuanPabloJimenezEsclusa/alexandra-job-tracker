package dev.jpje.jobtracker.server.config;

import java.time.Clock;

import dev.jpje.jobtracker.application.port.outbound.AuthenticateUserPort;
import dev.jpje.jobtracker.application.port.outbound.EventPublisher;
import dev.jpje.jobtracker.application.port.outbound.PasswordEncoderPort;
import dev.jpje.jobtracker.application.port.outbound.TokenGeneratorPort;
import dev.jpje.jobtracker.application.service.JobPostingService;
import dev.jpje.jobtracker.application.usecase.AnalyzeJobPostingUseCase;
import dev.jpje.jobtracker.application.usecase.AuthenticationUseCase;
import dev.jpje.jobtracker.application.usecase.GetAnalyticsUseCase;
import dev.jpje.jobtracker.application.usecase.ListJobPostingsUseCase;
import dev.jpje.jobtracker.application.usecase.ManageJobAnalysisUseCase;
import dev.jpje.jobtracker.application.usecase.ProcessJobPostingCreatedUseCase;
import dev.jpje.jobtracker.application.usecase.SubmitJobPostingUseCase;
import dev.jpje.jobtracker.application.usecase.TrackJobApplicationUseCase;
import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.port.inbound.AnalyzeJobPostingPort;
import dev.jpje.jobtracker.domain.port.inbound.AuthenticationPort;
import dev.jpje.jobtracker.domain.port.inbound.GetAnalyticsPort;
import dev.jpje.jobtracker.domain.port.inbound.ListJobPostingsPort;
import dev.jpje.jobtracker.domain.port.inbound.ManageJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.inbound.ProcessJobPostingCreatedPort;
import dev.jpje.jobtracker.domain.port.inbound.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.port.inbound.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.port.outbound.JobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.LoadJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.outbound.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.outbound.LoadUserPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobPostingPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveUserPort;
import dev.jpje.jobtracker.domain.service.AnalyticsCalculator;
import dev.jpje.jobtracker.server.usecase.TransactionalAuthenticationPort;
import dev.jpje.jobtracker.server.usecase.TransactionalSubmitJobPostingPort;
import dev.jpje.jobtracker.server.usecase.TransactionalTrackJobApplicationPort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
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
    final var delegate = new SubmitJobPostingUseCase(savePostingPort, jobPostingService, clock);
    return new TransactionalSubmitJobPostingPort(delegate, transactionTemplate, submitDurationTimer);
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
  AnalyticsCalculator analyticsCalculator() {
    return new AnalyticsCalculator();
  }

  @Bean
  GetAnalyticsPort getAnalyticsUseCase(final LoadJobApplicationPort loadPort,
                                       final AnalyticsCalculator analyticsCalculator) {
    return new GetAnalyticsUseCase(loadPort, analyticsCalculator);
  }

  @Bean
  TrackJobApplicationPort trackJobApplicationUseCase(
      final Clock clock,
      final SaveJobApplicationPort savePort,
      final LoadJobApplicationPort loadPort,
      final LoadJobPostingPort loadPostingPort,
      final EventPublisher eventPublisher,
      final Counter applicationCreatedCounter,
      final TransactionTemplate transactionTemplate) {
    final var delegate = new TrackJobApplicationUseCase(savePort, loadPort, loadPostingPort, clock, eventPublisher);
    return new TransactionalTrackJobApplicationPort(delegate, transactionTemplate, applicationCreatedCounter);
  }

  @Bean
  ProcessJobPostingCreatedPort processJobPostingCreatedUseCase(
      final Clock clock,
      final SaveJobApplicationPort saveApplicationPort,
      final JobAnalysisPort analysisPort,
      final SaveJobAnalysisPort saveAnalysisPort,
      final Counter applicationCreatedCounter) {
    final var impl = new ProcessJobPostingCreatedUseCase(saveApplicationPort, analysisPort,
      saveAnalysisPort, clock);
    return new ProcessJobPostingCreatedPort() {
      @Override
      public void createTracking(final JobPostingCreated event) {
        impl.createTracking(event);
        applicationCreatedCounter.increment();
      }

      @Override
      public void analyzePosting(final JobPostingCreated event) {
        impl.analyzePosting(event);
      }
    };
  }

  @Bean
  AuthenticationPort authenticationUseCase(
      final Clock clock,
      final SaveUserPort saveUserPort,
      final LoadUserPort loadUserPort,
      final TokenGeneratorPort tokenGenerator,
      final PasswordEncoderPort passwordEncoder,
      final AuthenticateUserPort authenticateUserPort,
      final EventPublisher eventPublisher,
      final TransactionTemplate transactionTemplate) {
    final var delegate = new AuthenticationUseCase(saveUserPort, loadUserPort, tokenGenerator,
      passwordEncoder, authenticateUserPort, clock, eventPublisher);
    return new TransactionalAuthenticationPort(delegate, transactionTemplate);
  }
}
