package com.jobtracker.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.jobtracker.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
  Optional<UserEntity> findByUsername(String username);
}
