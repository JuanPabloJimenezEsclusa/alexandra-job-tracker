package dev.jpje.jobtracker.api.dto;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;

public record JobAnalysisResponse(
    UUID id,
    UUID jobPostingId,
    String summary,
    String seniority,
    List<String> softSkills,
    List<String> technicalSkills,
    double fitScore,
    double companyRating,
    String companyType,
    double salaryMin,
    double salaryMax,
    String salaryCurrency,
    Instant createdAt) {

  public JobAnalysisResponse {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(jobPostingId, "jobPostingId must not be null");
    Objects.requireNonNull(summary, "summary must not be null");
    Objects.requireNonNull(seniority, "seniority must not be null");
    Objects.requireNonNull(softSkills, "soft skills must not be null");
    Objects.requireNonNull(technicalSkills, "technical skills must not be null");
    Objects.requireNonNull(companyType, "companyType must not be null");
    Objects.requireNonNull(salaryCurrency, "salaryCurrency must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
  }

  public static JobAnalysisResponse from(final JobAnalysisRecord record) {
    Objects.requireNonNull(record, "record must not be null");
    final var analysis = record.analysis();
    return new JobAnalysisResponse(
      record.id(),
      record.jobPostingId(),
      analysis.summary(),
      analysis.seniority(),
      analysis.softSkills(),
      analysis.technicalSkills(),
      analysis.fitScore(),
      analysis.companyRating(),
      analysis.companyType(),
      analysis.salaryMin(),
      analysis.salaryMax(),
      analysis.salaryCurrency(),
      record.createdAt());
  }
}
