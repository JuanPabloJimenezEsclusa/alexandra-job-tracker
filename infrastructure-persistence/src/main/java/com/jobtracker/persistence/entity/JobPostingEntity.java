package com.jobtracker.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "job_postings")
public class JobPostingEntity {
  @Id
  private UUID id;
  @Column(nullable = false)
  private UUID userId;
  @Column(nullable = false)
  private String url;
  @Column(nullable = false)
  private String source;
  @Column(nullable = false)
  private String title;
  @Column(nullable = false)
  private String company;
  @Column(columnDefinition = "TEXT")
  private String description;
  private Instant postedAt;

  public JobPostingEntity() {
  }

  public JobPostingEntity(UUID id, UUID userId, String url, String source, String title, String company, String description, Instant postedAt) {
    this.id = id;
    this.userId = userId;
    this.url = url;
    this.source = source;
    this.title = title;
    this.company = company;
    this.description = description;
    this.postedAt = postedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getUrl() {
    return url;
  }

  public String getSource() {
    return source;
  }

  public String getTitle() {
    return title;
  }

  public String getCompany() {
    return company;
  }

  public String getDescription() {
    return description;
  }

  public Instant getPostedAt() {
    return postedAt;
  }
}
