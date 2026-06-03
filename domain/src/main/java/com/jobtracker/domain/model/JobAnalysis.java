package com.jobtracker.domain.model;

import java.util.List;
import java.util.Objects;

/**
 * Analysis of a job posting including summary, skills, and fit score.
 */
public record JobAnalysis(
  String summary,
  List<String> skills,
  double fitScore) {

  /**
   * Instantiates a new Job analysis.
   */
  public JobAnalysis {
    Objects.requireNonNull(summary, "summary must not be null");
    Objects.requireNonNull(skills, "skills must not be null");
    if (fitScore < 0.0 || fitScore > 100.0) {
      throw new IllegalArgumentException("fitScore must be between 0.0 and 100.0, but was " + fitScore);
    }
  }
}
