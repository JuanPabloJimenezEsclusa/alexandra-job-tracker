package dev.jpje.jobtracker.api.dto;

import java.util.List;
import java.util.Objects;

import dev.jpje.jobtracker.domain.vo.JobAnalysis;

public record JobAnalysisResponse(
    String summary,
    List<String> skills,
    double fitScore) {

  public JobAnalysisResponse {
    Objects.requireNonNull(summary, "summary must not be null");
    Objects.requireNonNull(skills, "skills must not be null");
  }

  public static JobAnalysisResponse from(final JobAnalysis analysis) {
    Objects.requireNonNull(analysis, "analysis must not be null");
    return new JobAnalysisResponse(analysis.summary(), analysis.skills(), analysis.fitScore());
  }
}
