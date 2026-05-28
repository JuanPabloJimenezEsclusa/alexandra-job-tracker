package com.jobtracker.persistence.adapter;

import java.util.Optional;

import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.out.LoadUserPort;
import com.jobtracker.domain.port.out.SaveUserPort;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.persistence.mapper.UserMapper;
import com.jobtracker.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter for user persistence.
 */
@Component
@Transactional(readOnly = true)
public class UserPersistenceAdapter implements SaveUserPort, LoadUserPort {
  private final UserJpaRepository repository;
  private final UserMapper mapper = new UserMapper();

  /**
   * Creates an adapter backed by the given JPA repository.
   */
  public UserPersistenceAdapter(UserJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void save(User user) {
    repository.save(mapper.toEntity(user));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return repository.findByUsername(username).map(mapper::toDomain);
  }

  @Override
  public Optional<User> findById(UserId id) {
    return repository.findById(id.value()).map(mapper::toDomain);
  }
}
