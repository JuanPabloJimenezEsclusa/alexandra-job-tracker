package com.jobtracker.persistence.adapter;

import java.util.Optional;

import com.jobtracker.domain.model.User;
import com.jobtracker.domain.port.out.LoadUserPort;
import com.jobtracker.domain.port.out.SaveUserPort;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.persistence.mapper.UserMapper;
import com.jobtracker.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceAdapter implements SaveUserPort, LoadUserPort {
  private final UserJpaRepository repository;
  private final UserMapper mapper = new UserMapper();

  public UserPersistenceAdapter(UserJpaRepository repository) {
    this.repository = repository;
  }

  @Override
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
