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
@Table(name = "applications")
public class JobApplicationEntity {
  @Id
  private UUID id;
  @Column(nullable = false)
  private UUID userId;
  @Column(nullable = false)
  private String company;
  @Column(nullable = false)
  private String role;
  @Column(nullable = false)
  private String source;
  @Column(nullable = false)
  private String postingUrl;
  @Column(nullable = false)
  private String status;
  @Column(nullable = false)
  private Instant dateApplied;
  @Column(nullable = false)
  private Instant lastUpdated;
  @Nullable
  private String notes;
  @Version @Nullable
  private Long version;

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(final UUID userId) {
    this.userId = userId;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(final String company) {
    this.company = company;
  }

  public String getRole() {
    return role;
  }

  public void setRole(final String role) {
    this.role = role;
  }

  public String getSource() {
    return source;
  }

  public void setSource(final String source) {
    this.source = source;
  }

  public String getPostingUrl() {
    return postingUrl;
  }

  public void setPostingUrl(final String postingUrl) {
    this.postingUrl = postingUrl;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }

  public Instant getDateApplied() {
    return dateApplied;
  }

  public void setDateApplied(final Instant dateApplied) {
    this.dateApplied = dateApplied;
  }

  public Instant getLastUpdated() {
    return lastUpdated;
  }

  public void setLastUpdated(final Instant lastUpdated) {
    this.lastUpdated = lastUpdated;
  }

  @Nullable
  public String getNotes() {
    return notes;
  }

  public void setNotes(@Nullable final String notes) {
    this.notes = notes;
  }

  @Nullable
  public Long getVersion() {
    return version;
  }

  public void setVersion(@Nullable final Long version) {
    this.version = version;
  }
}
