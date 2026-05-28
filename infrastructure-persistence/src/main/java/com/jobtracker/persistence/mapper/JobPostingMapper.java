package com.jobtracker.persistence.mapper;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.persistence.entity.JobPostingEntity;

/**
 * Maps between JobPostingEntity and JobPosting domain model.
 */
public class JobPostingMapper {
  /**
   * Maps entity to domain model.
   */
  public JobPosting toDomain(final JobPostingEntity entity) {
    return new JobPosting(entity.getId(), new UserId(entity.getUserId()),
      entity.getUrl(), Source.valueOf(entity.getSource()), entity.getTitle(),
      entity.getCompany(), entity.getDescription(), entity.getPostedAt());
  }

  /**
   * Maps domain model to entity.
   */
  public JobPostingEntity toEntity(final JobPosting domain) {
    final var entity = new JobPostingEntity();
    entity.setId(domain.id());
    entity.setUserId(domain.userId().value());
    entity.setUrl(domain.url());
    entity.setSource(domain.source().name());
    entity.setTitle(domain.title());
    entity.setCompany(domain.company());
    entity.setDescription(domain.description());
    entity.setPostedAt(domain.postedAt());
    return entity;
  }
}
