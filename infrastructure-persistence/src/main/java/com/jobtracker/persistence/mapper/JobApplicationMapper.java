package com.jobtracker.persistence.mapper;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.persistence.entity.JobApplicationEntity;

public class JobApplicationMapper {
  public JobApplication toDomain(final JobApplicationEntity entity) {
    return new JobApplication(entity.getId(), new UserId(entity.getUserId()),
        entity.getCompany(), entity.getRole(), Source.valueOf(entity.getSource()),
        entity.getPostingUrl(), ApplicationStatus.valueOf(entity.getStatus()),
        entity.getDateApplied(), entity.getLastUpdated(), entity.getNotes());
  }
  public JobApplicationEntity toEntity(final JobApplication domain) {
    return new JobApplicationEntity(domain.id(), domain.userId().value(),
        domain.company(), domain.role(), domain.source().name(),
        domain.postingUrl(), domain.status().name(), domain.dateApplied(),
        domain.lastUpdated(), domain.notes());
  }
}
