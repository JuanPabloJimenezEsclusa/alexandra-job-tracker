package com.jobtracker.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * JPA entity for job postings.
 */
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

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public String getSource() {
    return source;
  }

  public void setSource(final String source) {
    this.source = source;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public String getCompany() {
    return company;
  }

  public void setCompany(final String company) {
    this.company = company;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public Instant getPostedAt() {
    return postedAt;
  }

  public void setPostedAt(final Instant postedAt) {
    this.postedAt = postedAt;
  }
}
