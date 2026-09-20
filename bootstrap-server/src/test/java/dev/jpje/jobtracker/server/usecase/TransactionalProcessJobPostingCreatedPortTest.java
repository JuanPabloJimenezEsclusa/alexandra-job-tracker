package dev.jpje.jobtracker.server.usecase;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.inbound.ProcessJobPostingCreatedPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.micrometer.core.instrument.Counter;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class TransactionalProcessJobPostingCreatedPortTest {

  private static final UserId USER_ID = UserId.generate();

  @Mock
  private ProcessJobPostingCreatedPort delegate;

  @Mock
  private PlatformTransactionManager transactionManager;

  @Mock
  private Counter applicationCreatedCounter;

  private TransactionalProcessJobPostingCreatedPort processPort;

  @BeforeEach
  void setUp() {
    processPort = new TransactionalProcessJobPostingCreatedPort(delegate,
      new TransactionTemplate(transactionManager), applicationCreatedCounter);
  }

  @Test
  void shouldRunCreateTrackingInOneTransactionAndIncrementCounterOnce() {
    // Given
    final var event = event();
    when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

    // When
    processPort.createTracking(event);

    // Then
    verify(transactionManager, description("createTracking opens a transaction")).getTransaction(any());
    verify(transactionManager, description("createTracking commits the transaction")).commit(any());
    verify(delegate, description("createTracking delegated to the use case")).createTracking(event);
    verify(applicationCreatedCounter, description("createTracking increments the application counter once"))
      .increment();
    verifyNoMoreInteractions(delegate, transactionManager, applicationCreatedCounter);
  }

  @Test
  void shouldDelegateAnalyzePostingWithoutTransaction() {
    // Given
    final var event = event();

    // When
    processPort.analyzePosting(event);

    // Then
    verify(delegate, description("analyzePosting delegated to the use case")).analyzePosting(event);
    verifyNoMoreInteractions(delegate);
    verifyNoInteractions(transactionManager, applicationCreatedCounter);
  }

  private static JobPostingCreated event() {
    final var posting = Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), USER_ID)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .set(field(JobPosting::url), Url.of("https://example.com/process-job"))
      .set(field(JobPosting::title), JobTitle.of("Engineer"))
      .set(field(JobPosting::company), CompanyName.of("Acme"))
      .set(field(JobPosting::description), "Software engineer role")
      .create();
    return JobPostingCreated.of(posting, Instant.EPOCH);
  }
}
