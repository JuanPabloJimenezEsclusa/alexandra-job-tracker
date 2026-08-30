package dev.jpje.jobtracker.persistence.mapper;

import dev.jpje.jobtracker.domain.model.User;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.domain.vo.UserRole;
import dev.jpje.jobtracker.domain.vo.Username;
import dev.jpje.jobtracker.persistence.entity.UserEntity;

public final class UserMapper {

  private UserMapper() {
  }

  public static User toDomain(final UserEntity entity) {
    return new User(new UserId(entity.getId()), Username.of(entity.getUsername()),
      entity.getPasswordHash(), UserRole.valueOf(entity.getRole()), entity.getCreatedAt());
  }

  public static UserEntity toEntity(final User domain) {
    return new UserEntity(domain.id().value(), domain.username().value(),
      domain.passwordHash(), domain.role().name(), domain.createdAt());
  }
}
