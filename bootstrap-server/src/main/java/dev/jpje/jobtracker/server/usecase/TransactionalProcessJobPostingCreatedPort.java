package dev.jpje.jobtracker.server.usecase;

import dev.jpje.jobtracker.domain.event.JobPostingCreated;
import dev.jpje.jobtracker.domain.port.inbound.ProcessJobPostingCreatedPort;
import io.micrometer.core.instrument.Counter;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalProcessJobPostingCreatedPort implements ProcessJobPostingCreatedPort {

  private final ProcessJobPostingCreatedPort delegate;
  private final TransactionTemplate transactionTemplate;
  private final Counter applicationCreatedCounter;

  public TransactionalProcessJobPostingCreatedPort(final ProcessJobPostingCreatedPort delegate,
                                                   final TransactionTemplate transactionTemplate,
                                                   final Counter applicationCreatedCounter) {
    this.delegate = delegate;
    this.transactionTemplate = transactionTemplate;
    this.applicationCreatedCounter = applicationCreatedCounter;
  }

  @Override
  public void createTracking(final JobPostingCreated event) {
    transactionTemplate.executeWithoutResult(_ -> delegate.createTracking(event));
    applicationCreatedCounter.increment();
  }

  @Override
  public void analyzePosting(final JobPostingCreated event) {
    delegate.analyzePosting(event);
  }
}
