package dev.jpje.jobtracker.persistence.mapper;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobPostingEntity;

public class JobPostingMapper {
  public JobPosting toDomain(final JobPostingEntity entity) {
    return new JobPosting(entity.getId(), new UserId(entity.getUserId()),
      Url.of(entity.getUrl()), Source.valueOf(entity.getSource()),
      JobTitle.of(entity.getTitle()), CompanyName.of(entity.getCompany()),
      entity.getDescription(), entity.getPostedAt());
  }

  public JobPostingEntity toEntity(final JobPosting domain) {
    final var entity = new JobPostingEntity();
    entity.setId(domain.id());
    entity.setUserId(domain.userId().value());
    entity.setUrl(domain.url().value());
    entity.setSource(domain.source().name());
    entity.setTitle(domain.title().value());
    entity.setCompany(domain.company().value());
    entity.setDescription(domain.description());
    entity.setPostedAt(domain.postedAt());
    return entity;
  }
}
