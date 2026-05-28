package dev.jpje.jobtracker.domain.vo;

import java.util.List;
import java.util.Objects;

public record JobAnalysis(
  String summary,
  List<String> skills,
  double fitScore) {

  public JobAnalysis {
    Objects.requireNonNull(summary, "summary must not be null");
    Objects.requireNonNull(skills, "skills must not be null");
    if (fitScore < 0.0 || fitScore > 100.0) {
      throw new IllegalArgumentException("fitScore must be between 0.0 and 100.0, but was " + fitScore);
    }
  }
}
