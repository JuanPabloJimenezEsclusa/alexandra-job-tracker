package com.jobtracker.persistence.mapper;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.persistence.entity.JobApplicationEntity;

/**
 * Maps between JobApplicationEntity and JobApplication domain model.
 */
public class JobApplicationMapper {
  /**
   * Maps entity to domain model.
   */
  public JobApplication toDomain(final JobApplicationEntity entity) {
    return new JobApplication(entity.getId(), new UserId(entity.getUserId()),
      entity.getCompany(), entity.getRole(), Source.valueOf(entity.getSource()),
      entity.getPostingUrl(), ApplicationStatus.valueOf(entity.getStatus()),
      entity.getDateApplied(), entity.getLastUpdated(), entity.getNotes());
  }

  /**
   * Maps domain model to entity.
   */
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
