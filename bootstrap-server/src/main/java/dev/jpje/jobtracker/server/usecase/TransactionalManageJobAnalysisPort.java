package dev.jpje.jobtracker.server.usecase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.port.inbound.ManageJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalManageJobAnalysisPort implements ManageJobAnalysisPort {

  private final ManageJobAnalysisPort delegate;
  private final TransactionTemplate transactionTemplate;

  public TransactionalManageJobAnalysisPort(final ManageJobAnalysisPort delegate,
                                            final TransactionTemplate transactionTemplate) {
    this.delegate = delegate;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public Optional<JobAnalysisRecord> findByIdForUser(final UserId userId, final UUID id) {
    return delegate.findByIdForUser(userId, id);
  }

  @Override
  public List<JobAnalysisRecord> findByUserId(final UserId userId) {
    return delegate.findByUserId(userId);
  }

  @Override
  public void delete(final UUID id) {
    transactionTemplate.executeWithoutResult(_ -> delegate.delete(id));
  }
}
