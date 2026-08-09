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

  public static JobAnalysisEntity toEntity(final JobAnalysisRecord record) {
    final var entity = new JobAnalysisEntity();
    entity.setId(record.id());
    entity.setJobPostingId(record.jobPostingId());
    entity.setUserId(record.userId().value());
    entity.setSummary(record.analysis().summary());
    entity.setSeniority(record.analysis().seniority());
    entity.setSoftSkills(toCsv(record.analysis().softSkills()));
    entity.setTechnicalSkills(toCsv(record.analysis().technicalSkills()));
    entity.setFitScore(record.analysis().fitScore());
    entity.setCompanyRating(record.analysis().companyRating());
    entity.setCompanyType(record.analysis().companyType());
    entity.setSalaryMin(record.analysis().salaryMin());
    entity.setSalaryMax(record.analysis().salaryMax());
    entity.setSalaryCurrency(record.analysis().salaryCurrency());
    entity.setCreatedAt(record.createdAt());
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
