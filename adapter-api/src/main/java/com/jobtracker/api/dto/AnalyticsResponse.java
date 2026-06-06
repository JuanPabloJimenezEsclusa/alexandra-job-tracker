package com.jobtracker.api.dto;

import java.util.Objects;

import com.jobtracker.domain.vo.Analytics;

/**
 * API response for analytics queries.
 */
public record AnalyticsResponse(int totalApplications, StatusCounts perStatus, double conversionRate) {

  /**
   * Maps a domain Analytics to an API response DTO.
   */
  public static AnalyticsResponse from(final Analytics analytics) {
    Objects.requireNonNull(analytics, "analytics must not be null");
    return new AnalyticsResponse(
      analytics.totalApplications(),
      StatusCounts.fromPerStatus(analytics.perStatus()),
      analytics.conversionRate());
  }
}
