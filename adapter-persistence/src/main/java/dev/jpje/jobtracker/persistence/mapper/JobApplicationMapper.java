package dev.jpje.jobtracker.persistence.mapper;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobApplicationEntity;

public class JobApplicationMapper {
  public JobApplication toDomain(final JobApplicationEntity entity) {
    return new JobApplication(entity.getId(), new UserId(entity.getUserId()),
      entity.getCompany(), entity.getRole(), Source.valueOf(entity.getSource()),
      entity.getPostingUrl(), ApplicationStatus.valueOf(entity.getStatus()),
      entity.getDateApplied(), entity.getLastUpdated(), entity.getNotes());
  }

  @SuppressWarnings("java:S4449") // false positives
  public JobApplicationEntity toEntity(final JobApplication domain) {
    final var entity = new JobApplicationEntity();
    entity.setId(domain.id());
    entity.setUserId(domain.userId().value());
    entity.setCompany(domain.company());
    entity.setRole(domain.role());
    entity.setSource(domain.source().name());

    if (domain.postingUrl() != null) {
      entity.setPostingUrl(domain.postingUrl());
    }

    entity.setStatus(domain.status().name());
    entity.setDateApplied(domain.dateApplied());
    entity.setLastUpdated(domain.lastUpdated());

    if  (domain.notes() != null) {
      entity.setNotes(domain.notes());
    }
    return entity;
  }
}
