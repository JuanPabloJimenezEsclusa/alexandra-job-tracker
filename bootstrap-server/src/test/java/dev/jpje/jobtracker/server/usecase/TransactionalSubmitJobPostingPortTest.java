package dev.jpje.jobtracker.server.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.inbound.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
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
class TransactionalSubmitJobPostingPortTest {

  private static final UserId USER_ID = UserId.generate();
  private static final Url URL = Url.of("https://example.com/submit-job");
  private static final JobTitle TITLE = JobTitle.of("Engineer");
  private static final CompanyName COMPANY = CompanyName.of("Acme");
  private static final String DESCRIPTION = "Software engineer role";
  private static final String FAILURE = "Simulated submission failure";

  @Mock
  private SubmitJobPostingPort delegate;

  @Mock
  private PlatformTransactionManager transactionManager;

  private Timer submitDurationTimer;

  private TransactionalSubmitJobPostingPort submitPort;

  @BeforeEach
  void setUp() {
    submitDurationTimer = new SimpleMeterRegistry().timer("submit.duration");
    submitPort = new TransactionalSubmitJobPostingPort(delegate,
      new TransactionTemplate(transactionManager), submitDurationTimer);
  }

  @Test
  void shouldRunSubmitInOneTransactionAndRecordDuration() {
    // Given
    final var posting = posting();
    when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    when(delegate.submit(USER_ID, URL, TITLE, COMPANY, DESCRIPTION, Source.LINKEDIN)).thenReturn(posting);

    // When
    final var result = submitPort.submit(USER_ID, URL, TITLE, COMPANY, DESCRIPTION, Source.LINKEDIN);

    // Then
    assertThat(result).isSameAs(posting);
    verify(transactionManager, description("submit opens a transaction")).getTransaction(any());
    verify(transactionManager, description("submit commits the transaction")).commit(any());
    verify(delegate, description("submit delegated to the use case"))
      .submit(USER_ID, URL, TITLE, COMPANY, DESCRIPTION, Source.LINKEDIN);
    assertThat(submitDurationTimer.count())
      .as("submit duration recorded after the transaction")
      .isEqualTo(1);
    verifyNoMoreInteractions(delegate, transactionManager);
  }

  @Test
  void shouldRecordDurationWhenDelegateThrows() {
    // Given
    when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
    when(delegate.submit(USER_ID, URL, TITLE, COMPANY, DESCRIPTION, Source.LINKEDIN))
      .thenThrow(new IllegalStateException(FAILURE));

    // When, then
    assertThatThrownBy(() -> submitPort.submit(USER_ID, URL, TITLE, COMPANY, DESCRIPTION, Source.LINKEDIN))
      .as("submission failure propagates")
      .isInstanceOf(IllegalStateException.class)
      .hasMessage(FAILURE);
    assertThat(submitDurationTimer.count())
      .as("duration recorded even when the delegate throws")
      .isEqualTo(1);
    verify(transactionManager, description("failed submit rolls back the transaction")).rollback(any());
  }

  private static JobPosting posting() {
    return Instancio.of(JobPosting.class)
      .set(field(JobPosting::userId), USER_ID)
      .set(field(JobPosting::url), URL)
      .set(field(JobPosting::title), TITLE)
      .set(field(JobPosting::company), COMPANY)
      .set(field(JobPosting::description), DESCRIPTION)
      .set(field(JobPosting::source), Source.LINKEDIN)
      .create();
  }
}
