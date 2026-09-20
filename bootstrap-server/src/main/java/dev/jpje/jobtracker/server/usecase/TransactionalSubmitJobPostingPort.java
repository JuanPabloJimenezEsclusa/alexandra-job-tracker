package dev.jpje.jobtracker.server.usecase;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.inbound.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import io.micrometer.core.instrument.Timer;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalSubmitJobPostingPort implements SubmitJobPostingPort {

  private final SubmitJobPostingPort delegate;
  private final TransactionTemplate transactionTemplate;
  private final Timer submitDurationTimer;

  public TransactionalSubmitJobPostingPort(final SubmitJobPostingPort delegate,
                                           final TransactionTemplate transactionTemplate,
                                           final Timer submitDurationTimer) {
    this.delegate = delegate;
    this.transactionTemplate = transactionTemplate;
    this.submitDurationTimer = submitDurationTimer;
  }

  @Override
  public JobPosting submit(final UserId userId,
                           final Url url,
                           final JobTitle title,
                           final CompanyName company,
                           final String description,
                           final Source source) {
    final var sample = Timer.start();
    try {
      return transactionTemplate.execute(_ -> delegate.submit(userId, url, title, company, description, source));
    } finally {
      sample.stop(submitDurationTimer);
    }
  }
}
