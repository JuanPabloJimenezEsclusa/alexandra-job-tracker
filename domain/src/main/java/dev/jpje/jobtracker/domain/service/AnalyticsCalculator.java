package dev.jpje.jobtracker.domain.service;

import java.util.EnumMap;
import java.util.List;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.Analytics;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;

public class AnalyticsCalculator {
  public Analytics calculate(final List<JobApplication> applications) {
    final var perStatus = new EnumMap<ApplicationStatus, Integer>(ApplicationStatus.class);
    for (final var status : ApplicationStatus.values()) {
      perStatus.put(status, 0);
    }
    for (final var app : applications) {
      perStatus.merge(app.status(), 1, Integer::sum);
    }
    return new Analytics(perStatus);
  }
}
