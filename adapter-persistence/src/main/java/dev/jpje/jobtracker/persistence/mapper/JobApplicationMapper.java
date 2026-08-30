package dev.jpje.jobtracker.persistence.mapper;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobApplicationEntity;

public final class JobApplicationMapper {

  private JobApplicationMapper() {
  }

  public static JobApplication toDomain(final JobApplicationEntity entity) {
    return new JobApplication(entity.getId(), new UserId(entity.getUserId()),
      entity.getJobPostingId(),
      ApplicationStatus.valueOf(entity.getStatus()),
      entity.getDateApplied(), entity.getLastUpdated(),
      entity.getNotes() != null ? Notes.of(entity.getNotes()) : null,
      entity.getVersion() != null ? entity.getVersion() : null);
  }

  @SuppressWarnings("java:S4449") // false positives
  public static JobApplicationEntity toEntity(final JobApplication domain) {
    final var entity = new JobApplicationEntity();
    entity.setId(domain.id());
    entity.setVersion(domain.version());
    entity.setUserId(domain.userId().value());
    entity.setJobPostingId(domain.jobPostingId());
    entity.setStatus(domain.status().name());
    entity.setDateApplied(domain.dateApplied());
    entity.setLastUpdated(domain.lastUpdated());

    final var notes = domain.notes();
    if (notes != null && !notes.isEmpty()) {
      entity.setNotes(notes.value());
    }
    return entity;
  }
}
