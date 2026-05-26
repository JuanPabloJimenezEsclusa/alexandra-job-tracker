package com.jobtracker.persistence.mapper;

import com.jobtracker.domain.model.User;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.persistence.entity.UserEntity;

public class UserMapper {
  public User toDomain(final UserEntity entity) {
    return new User(new UserId(entity.getId()), entity.getUsername(), entity.getPasswordHash(), entity.getCreatedAt());
  }
  public UserEntity toEntity(final User domain) {
    return new UserEntity(domain.id().value(), domain.username(), domain.passwordHash(), domain.createdAt());
  }
}
