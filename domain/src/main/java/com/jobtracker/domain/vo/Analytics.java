package com.jobtracker.domain.vo;

import java.util.Map;
import java.util.Objects;

/**
 * Analytics data including per-status counts for job applications.
 */
public record Analytics(Map<ApplicationStatus, Integer> perStatus) {

  /**
   * Validates that the map is not null and all counts are non-negative.
   */
  public Analytics {
    Objects.requireNonNull(perStatus, "perStatus must not be null");
    for (final var entry : perStatus.entrySet()) {
      if (entry.getValue() < 0) {
        throw new IllegalArgumentException("Count for " + entry.getKey() + " must not be negative");
      }
    }
  }

  /**
   * Returns the total number of tracked applications.
   */
  public int totalApplications() {
    return perStatus.values().stream().mapToInt(Integer::intValue).sum();
  }

  /**
   * Returns the conversion rate as a percentage of accepted over total applications.
   */
  public double conversionRate() {
    final int total = totalApplications();
    return total == 0 ? 0 : (double) perStatus.getOrDefault(ApplicationStatus.ACCEPTED, 0) / total * 100;
  }
}
