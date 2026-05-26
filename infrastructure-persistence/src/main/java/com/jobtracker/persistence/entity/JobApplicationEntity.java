package com.jobtracker.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
  private String postingUrl;
  @Column(nullable = false)
  private String status;
  @Column(nullable = false)
  private Instant dateApplied;
  @Column(nullable = false)
  private Instant lastUpdated;
  private String notes;

  public JobApplicationEntity() {
  }

  public JobApplicationEntity(UUID id, UUID userId, String company, String role, String source,
                               String postingUrl, String status, Instant dateApplied,
                               Instant lastUpdated, String notes) {
    this.id = id;
    this.userId = userId;
    this.company = company;
    this.role = role;
    this.source = source;
    this.postingUrl = postingUrl;
    this.status = status;
    this.dateApplied = dateApplied;
    this.lastUpdated = lastUpdated;
    this.notes = notes;
  }

  public UUID getId() { return id; }
  public UUID getUserId() { return userId; }
  public String getCompany() { return company; }
  public String getRole() { return role; }
  public String getSource() { return source; }
  public String getPostingUrl() { return postingUrl; }
  public String getStatus() { return status; }
  public Instant getDateApplied() { return dateApplied; }
  public Instant getLastUpdated() { return lastUpdated; }
  public String getNotes() { return notes; }
}
