package com.jobtracker.persistence.mapper;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.persistence.entity.JobPostingEntity;

public class JobPostingMapper {
  public JobPosting toDomain(final JobPostingEntity entity) {
    return new JobPosting(entity.getId(), new UserId(entity.getUserId()),
        entity.getUrl(), Source.valueOf(entity.getSource()), entity.getTitle(),
        entity.getCompany(), entity.getDescription(), entity.getPostedAt());
  }
  public JobPostingEntity toEntity(final JobPosting domain) {
    return new JobPostingEntity(domain.id(), domain.userId().value(),
        domain.url(), domain.source().name(), domain.title(),
        domain.company(), domain.description(), domain.postedAt());
  }
}
