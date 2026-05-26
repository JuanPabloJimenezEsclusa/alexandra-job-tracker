package com.jobtracker.domain.service;

import java.util.*;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.Analytics;
import com.jobtracker.domain.vo.ApplicationStatus;

public class AnalyticsCalculator {
  public Analytics calculate(final List<JobApplication> applications) {
    final Map<ApplicationStatus, Integer> perStatus = new EnumMap<>(ApplicationStatus.class);
    for (var status : ApplicationStatus.values()) perStatus.put(status, 0);
    for (var app : applications) perStatus.merge(app.status(), 1, Integer::sum);
    return new Analytics(perStatus);
  }
}
