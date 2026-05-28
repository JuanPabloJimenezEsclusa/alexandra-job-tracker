package dev.jpje.jobtracker.domain.vo;

import java.util.Map;
import java.util.Objects;

public record Analytics(Map<ApplicationStatus, Integer> perStatus) {

  public Analytics {
    Objects.requireNonNull(perStatus, "perStatus must not be null");
    for (final var entry : perStatus.entrySet()) {
      if (entry.getValue() < 0) {
        throw new IllegalArgumentException("Count for " + entry.getKey() + " must not be negative");
      }
    }
  }

  public int totalApplications() {
    return perStatus.values().stream().mapToInt(Integer::intValue).sum();
  }

  public double conversionRate() {
    final int total = totalApplications();
    return total == 0 ? 0 : (double) perStatus.getOrDefault(ApplicationStatus.ACCEPTED, 0) / total * 100;
  }
}
