package dev.jpje.jobtracker.server.usecase;

import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.inbound.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.micrometer.core.instrument.Counter;
import org.jspecify.annotations.Nullable;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalTrackJobApplicationPort implements TrackJobApplicationPort {

  private final TrackJobApplicationPort delegate;
  private final TransactionTemplate transactionTemplate;
  private final Counter applicationCreatedCounter;

  public TransactionalTrackJobApplicationPort(final TrackJobApplicationPort delegate,
                                              final TransactionTemplate transactionTemplate,
                                              final Counter applicationCreatedCounter) {
    this.delegate = delegate;
    this.transactionTemplate = transactionTemplate;
    this.applicationCreatedCounter = applicationCreatedCounter;
  }

  @Override
  public JobApplication create(final UserId userId,
                               final UUID jobPostingId,
                               @Nullable final Notes notes) {
    final var result = transactionTemplate.execute(_ -> delegate.create(userId, jobPostingId, notes));
    applicationCreatedCounter.increment();
    return result;
  }

  @Override
  public JobApplication updateStatus(final UserId userId,
                                     final UUID applicationId,
                                     final ApplicationStatus newStatus,
                                     @Nullable final Notes notes) {
    return transactionTemplate.execute(_ -> delegate.updateStatus(userId, applicationId, newStatus, notes));
  }

  @Override
  public List<JobApplication> list(final UserId userId, @Nullable final ApplicationStatus status) {
    return delegate.list(userId, status);
  }

  @Override
  public void delete(final UserId userId, final UUID applicationId) {
    transactionTemplate.executeWithoutResult(_ -> delegate.delete(userId, applicationId));
  }
}
