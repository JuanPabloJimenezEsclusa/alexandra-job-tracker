package com.jobtracker.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity for users.
 */
@Entity
@Table(name = "users")
public class UserEntity {
  @Id
  private UUID id;
  @Column(unique = true, nullable = false)
  private String username;
  @Column(nullable = false)
  private String passwordHash;
  @Column(nullable = false)
  private Instant createdAt;

  /**
   * UserEntity.
   */
  public UserEntity() {
  }

  /**
   * UserEntity.
   */
  public UserEntity(
    final UUID id,
    final String username,
    final String passwordHash,
    final Instant createdAt) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
