package com.jobtracker.domain.model;

import java.util.List;

/**
 * Analysis of a job posting including summary, skills, and fit score.
 */
public record JobAnalysis(
  String summary,
  List<String> skills,
  double fitScore) {
}
