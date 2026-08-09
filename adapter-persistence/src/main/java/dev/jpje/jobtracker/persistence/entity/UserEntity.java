package dev.jpje.jobtracker.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

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
  @Version @Nullable
  private Long version;

  public UserEntity() {
  }

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

  public @Nullable Long getVersion() {
    return version;
  }
}
