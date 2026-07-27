package dev.jpje.jobtracker.api.dto;

import java.util.Map;

import dev.jpje.jobtracker.domain.vo.ApplicationStatus;

public record StatusCounts(
  int saved,
  int applied,
  int interviewing,
  int offer,
  int accepted,
  int rejected,
  int withdrawn
) {
  public static StatusCounts fromPerStatus(final Map<ApplicationStatus, Integer> perStatus) {
    return new StatusCounts(
      perStatus.getOrDefault(ApplicationStatus.SAVED, 0),
      perStatus.getOrDefault(ApplicationStatus.APPLIED, 0),
      perStatus.getOrDefault(ApplicationStatus.INTERVIEWING, 0),
      perStatus.getOrDefault(ApplicationStatus.OFFER, 0),
      perStatus.getOrDefault(ApplicationStatus.ACCEPTED, 0),
      perStatus.getOrDefault(ApplicationStatus.REJECTED, 0),
      perStatus.getOrDefault(ApplicationStatus.WITHDRAWN, 0));
  }
}
