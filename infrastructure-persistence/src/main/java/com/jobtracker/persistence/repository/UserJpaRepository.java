package com.jobtracker.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.jobtracker.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for users.
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
  /**
   * Finds a user by username.
   */
  Optional<UserEntity> findByUsername(String username);
}
