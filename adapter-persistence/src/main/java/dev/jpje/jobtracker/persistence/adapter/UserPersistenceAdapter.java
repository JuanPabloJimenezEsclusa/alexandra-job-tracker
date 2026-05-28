package dev.jpje.jobtracker.persistence.adapter;

import java.util.Optional;

import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.port.out.LoadUserPort;
import dev.jpje.jobtracker.domain.port.out.SaveUserPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.mapper.UserMapper;
import dev.jpje.jobtracker.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class UserPersistenceAdapter implements SaveUserPort, LoadUserPort {
  private final UserJpaRepository repository;
  private final UserMapper mapper = new UserMapper();

  public UserPersistenceAdapter(final UserJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void save(final User user) {
    final var entity = mapper.toEntity(user);
    repository.save(entity);
  }

  @Override
  public Optional<User> findByUsername(final String username) {
    return repository.findByUsername(username).map(mapper::toDomain);
  }

  @Override
  public Optional<User> findById(final UserId id) {
    return repository.findById(id.value()).map(mapper::toDomain);
  }
}
