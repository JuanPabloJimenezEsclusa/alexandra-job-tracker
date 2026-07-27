package dev.jpje.jobtracker.persistence.mapper;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobPostingEntity;

public class JobPostingMapper {
  public JobPosting toDomain(final JobPostingEntity entity) {
    return new JobPosting(entity.getId(), new UserId(entity.getUserId()),
      entity.getUrl(), Source.valueOf(entity.getSource()), entity.getTitle(),
      entity.getCompany(), entity.getDescription(), entity.getPostedAt());
  }

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
