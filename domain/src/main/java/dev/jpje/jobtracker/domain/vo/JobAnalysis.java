package dev.jpje.jobtracker.domain.vo;

import java.util.List;
import java.util.Objects;

public record JobAnalysis(
  String summary,
  String seniority,
  List<String> softSkills,
  List<String> technicalSkills,
  double fitScore,
  double companyRating,
  String companyType,
  double salaryMin,
  double salaryMax,
  String salaryCurrency) {

  public JobAnalysis {
    Objects.requireNonNull(summary, "summary must not be null");
    Objects.requireNonNull(seniority, "seniority must not be null");
    Objects.requireNonNull(softSkills, "soft skills must not be null");
    Objects.requireNonNull(technicalSkills, "technical skills must not be null");
    Objects.requireNonNull(companyType, "companyType must not be null");
    Objects.requireNonNull(salaryCurrency, "salaryCurrency must not be null");
    if (fitScore < 0.0 || fitScore > 100.0) {
      throw new IllegalArgumentException("fitScore must be between 0.0 and 100.0, but was " + fitScore);
    }
    if (companyRating < 0.0 || companyRating > 5.0) {
      throw new IllegalArgumentException("companyRating must be between 0.0 and 5.0, but was " + companyRating);
    }
    if (salaryMin < 0.0 || salaryMax < 0.0) {
      throw new IllegalArgumentException("salary must not be negative");
    }
    if (salaryMax < salaryMin) {
      throw new IllegalArgumentException("salaryMax must be greater than or equal to salaryMin");
    }
  }
}
