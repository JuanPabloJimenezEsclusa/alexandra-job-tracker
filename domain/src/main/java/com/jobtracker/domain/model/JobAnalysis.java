package com.jobtracker.domain.model;

import java.util.List;

public record JobAnalysis(
  String summary,
  List<String> skills,
  double fitScore) {}
