package dev.jpje.jobtracker.persistence.mapper;

import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.UserEntity;

public class UserMapper {
  public User toDomain(final UserEntity entity) {
    return new User(new UserId(entity.getId()), entity.getUsername(), entity.getPasswordHash(), entity.getCreatedAt());
  }

  public UserEntity toEntity(final User domain) {
    return new UserEntity(domain.id().value(), domain.username(), domain.passwordHash(), domain.createdAt());
  }
}
