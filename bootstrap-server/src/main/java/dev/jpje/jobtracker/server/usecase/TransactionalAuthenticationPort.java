package dev.jpje.jobtracker.server.usecase;

import java.util.Optional;

import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.inbound.AuthenticationPort;
import dev.jpje.jobtracker.domain.vo.AuthPayload;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;
import org.springframework.transaction.support.TransactionTemplate;

public class TransactionalAuthenticationPort implements AuthenticationPort {

  private final AuthenticationPort delegate;
  private final TransactionTemplate transactionTemplate;

  public TransactionalAuthenticationPort(final AuthenticationPort delegate,
                                         final TransactionTemplate transactionTemplate) {
    this.delegate = delegate;
    this.transactionTemplate = transactionTemplate;
  }

  @Override
  public AuthPayload register(final Username username, final String password, final UserRole role) {
    return transactionTemplate.execute(_ -> delegate.register(username, password, role));
  }

  @Override
  public AuthPayload login(final Username username, final String password) {
    return delegate.login(username, password);
  }

  @Override
  public Optional<User> getCurrentUser(final UserId userId) {
    return delegate.getCurrentUser(userId);
  }
}
