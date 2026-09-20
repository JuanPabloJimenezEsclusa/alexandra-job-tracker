package dev.jpje.jobtracker.server.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.port.inbound.ManageJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(MockitoExtension.class)
class TransactionalManageJobAnalysisPortTest {

  private static final UserId USER_ID = UserId.generate();
  private static final UUID ANALYSIS_ID = UUID.randomUUID();

  @Mock
  private ManageJobAnalysisPort delegate;

  @Mock
  private PlatformTransactionManager transactionManager;

  private TransactionalManageJobAnalysisPort analysisPort;

  @BeforeEach
  void setUp() {
    analysisPort = new TransactionalManageJobAnalysisPort(delegate, new TransactionTemplate(transactionManager));
  }

  private static Stream<Arguments> readOperations() {
    return Stream.of(
      arguments(
        named("findByIdForUser", "findByIdForUser"),
        (Consumer<TransactionalManageJobAnalysisPort>) port -> port.findByIdForUser(USER_ID, ANALYSIS_ID),
        (Consumer<ManageJobAnalysisPort>) given -> {
          final var record = analysis();
          when(given.findByIdForUser(USER_ID, ANALYSIS_ID)).thenReturn(Optional.of(record));
        },
        (Consumer<ManageJobAnalysisPort>) given ->
          verify(given, description("findByIdForUser delegated to the use case"))
            .findByIdForUser(USER_ID, ANALYSIS_ID)),
      arguments(
        named("findByUserId", "findByUserId"),
        (Consumer<TransactionalManageJobAnalysisPort>) port -> port.findByUserId(USER_ID),
        (Consumer<ManageJobAnalysisPort>) given -> {
          final var records = List.of(analysis());
          when(given.findByUserId(USER_ID)).thenReturn(records);
        },
        (Consumer<ManageJobAnalysisPort>) given ->
          verify(given, description("findByUserId delegated to the use case")).findByUserId(USER_ID))
    );
  }

  @ParameterizedTest(name = "{0} is delegated without a transaction")
  @MethodSource("readOperations")
  void shouldDelegateReadOperationWithoutTransaction(final String operationName,
                                                     final Consumer<TransactionalManageJobAnalysisPort> operation,
                                                     final Consumer<ManageJobAnalysisPort> stubbing,
                                                     final Consumer<ManageJobAnalysisPort> delegateVerification) {
    // Given
    stubbing.accept(delegate);

    // When
    operation.accept(analysisPort);

    // Then
    delegateVerification.accept(delegate);
    verifyNoMoreInteractions(delegate);
    verifyNoInteractions(transactionManager);
  }

  @Test
  void shouldRunDeleteInOneTransaction() {
    // Given
    when(transactionManager.getTransaction(any())).thenReturn(new SimpleTransactionStatus());

    // When
    analysisPort.delete(ANALYSIS_ID);

    // Then
    verify(transactionManager, description("delete opens a transaction")).getTransaction(any());
    verify(transactionManager, description("delete commits the transaction")).commit(any());
    verify(delegate, description("delete delegated to the use case")).delete(ANALYSIS_ID);
    verifyNoMoreInteractions(delegate, transactionManager);
  }

  private static JobAnalysisRecord analysis() {
    final var jobAnalysis = Instancio.of(JobAnalysis.class)
      .set(field(JobAnalysis::summary), "Mocked analysis")
      .set(field(JobAnalysis::fitScore), 85.0)
      .set(field(JobAnalysis::companyRating), 4.2)
      .set(field(JobAnalysis::companyType), "enterprise")
      .set(field(JobAnalysis::salaryMin), 90000.0)
      .set(field(JobAnalysis::salaryMax), 130000.0)
      .set(field(JobAnalysis::salaryCurrency), "USD")
      .create();
    return Instancio.of(JobAnalysisRecord.class)
      .set(field(JobAnalysisRecord::id), ANALYSIS_ID)
      .set(field(JobAnalysisRecord::userId), USER_ID)
      .set(field(JobAnalysisRecord::analysis), jobAnalysis)
      .create();
  }
}
