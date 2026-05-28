package dev.jpje.jobtracker.api.dto;

import java.util.Objects;

import dev.jpje.jobtracker.domain.vo.Analytics;

public record AnalyticsResponse(int totalApplications, StatusCounts perStatus, double conversionRate) {

  public static AnalyticsResponse from(final Analytics analytics) {
    Objects.requireNonNull(analytics, "analytics must not be null");
    return new AnalyticsResponse(
      analytics.totalApplications(),
      StatusCounts.fromPerStatus(analytics.perStatus()),
      analytics.conversionRate());
  }
}
