package dev.jpje.jobtracker.persistence.mapper;

import java.util.Arrays;
import java.util.List;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobAnalysisEntity;

public final class JobAnalysisMapper {

  private JobAnalysisMapper() {
  }

  public static JobAnalysisRecord toDomain(final JobAnalysisEntity entity) {
    return new JobAnalysisRecord(
      entity.getId(),
      entity.getJobPostingId(),
      new UserId(entity.getUserId()),
      new JobAnalysis(
        entity.getSummary(),
        entity.getSeniority(),
        fromCsv(entity.getSoftSkills()),
        fromCsv(entity.getTechnicalSkills()),
        entity.getFitScore(),
        entity.getCompanyRating(),
        entity.getCompanyType(),
        entity.getSalaryMin(),
        entity.getSalaryMax(),
        entity.getSalaryCurrency()),
      entity.getCreatedAt());
  }

  public static JobAnalysisEntity toEntity(final JobAnalysisRecord jobAnalysisRecord) {
    final var entity = new JobAnalysisEntity();
    entity.setId(jobAnalysisRecord.id());
    entity.setJobPostingId(jobAnalysisRecord.jobPostingId());
    entity.setUserId(jobAnalysisRecord.userId().value());
    entity.setSummary(jobAnalysisRecord.analysis().summary());
    entity.setSeniority(jobAnalysisRecord.analysis().seniority());
    entity.setSoftSkills(toCsv(jobAnalysisRecord.analysis().softSkills()));
    entity.setTechnicalSkills(toCsv(jobAnalysisRecord.analysis().technicalSkills()));
    entity.setFitScore(jobAnalysisRecord.analysis().fitScore());
    entity.setCompanyRating(jobAnalysisRecord.analysis().companyRating());
    entity.setCompanyType(jobAnalysisRecord.analysis().companyType());
    entity.setSalaryMin(jobAnalysisRecord.analysis().salaryMin());
    entity.setSalaryMax(jobAnalysisRecord.analysis().salaryMax());
    entity.setSalaryCurrency(jobAnalysisRecord.analysis().salaryCurrency());
    entity.setCreatedAt(jobAnalysisRecord.createdAt());
    return entity;
  }

  private static List<String> fromCsv(final String value) {
    if (value.isBlank()) {
      return List.of();
    }
    return Arrays.stream(value.split(","))
      .map(String::trim)
      .filter(s -> !s.isEmpty())
      .toList();
  }

  private static String toCsv(final List<String> skills) {
    return skills.isEmpty() ? "" : String.join(",", skills);
  }
}
