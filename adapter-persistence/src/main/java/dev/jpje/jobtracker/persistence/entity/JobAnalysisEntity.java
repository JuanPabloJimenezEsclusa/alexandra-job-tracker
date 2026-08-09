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
@Table(name = "job_analyses")
public class JobAnalysisEntity {
  @Id
  private UUID id;
  @Column(nullable = false)
  private UUID jobPostingId;
  @Column(nullable = false)
  private UUID userId;
  @Column(nullable = false)
  private String summary;
  @Column(nullable = false)
  private String seniority;
  @Column(nullable = false)
  private String softSkills;
  @Column(nullable = false)
  private String technicalSkills;
  @Column(nullable = false)
  private double fitScore;
  @Column(nullable = false)
  private double companyRating;
  @Column(nullable = false)
  private String companyType;
  @Column(nullable = false)
  private double salaryMin;
  @Column(nullable = false)
  private double salaryMax;
  @Column(nullable = false)
  private String salaryCurrency;
  @Column(nullable = false)
  private Instant createdAt;
  @Version @Nullable
  private Long version;

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public UUID getJobPostingId() {
    return jobPostingId;
  }

  public void setJobPostingId(final UUID jobPostingId) {
    this.jobPostingId = jobPostingId;
  }

  public UUID getUserId() {
    return userId;
  }

  public void setUserId(final UUID userId) {
    this.userId = userId;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(final String summary) {
    this.summary = summary;
  }

  public String getSeniority() {
    return seniority;
  }

  public void setSeniority(final String seniority) {
    this.seniority = seniority;
  }

  public String getSoftSkills() {
    return softSkills;
  }

  public void setSoftSkills(final String softSkills) {
    this.softSkills = softSkills;
  }

  public String getTechnicalSkills() {
    return technicalSkills;
  }

  public void setTechnicalSkills(final String technicalSkills) {
    this.technicalSkills = technicalSkills;
  }

  public double getFitScore() {
    return fitScore;
  }

  public void setFitScore(final double fitScore) {
    this.fitScore = fitScore;
  }

  public double getCompanyRating() {
    return companyRating;
  }

  public void setCompanyRating(final double companyRating) {
    this.companyRating = companyRating;
  }

  public String getCompanyType() {
    return companyType;
  }

  public void setCompanyType(final String companyType) {
    this.companyType = companyType;
  }

  public double getSalaryMin() {
    return salaryMin;
  }

  public void setSalaryMin(final double salaryMin) {
    this.salaryMin = salaryMin;
  }

  public double getSalaryMax() {
    return salaryMax;
  }

  public void setSalaryMax(final double salaryMax) {
    this.salaryMax = salaryMax;
  }

  public String getSalaryCurrency() {
    return salaryCurrency;
  }

  public void setSalaryCurrency(final String salaryCurrency) {
    this.salaryCurrency = salaryCurrency;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public @Nullable Long getVersion() {
    return version;
  }
}
