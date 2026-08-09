package dev.jpje.jobtracker.persistence.mapper;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.RoleName;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobApplicationEntity;

public final class JobApplicationMapper {

  private JobApplicationMapper() {
  }

  public static JobApplication toDomain(final JobApplicationEntity entity) {
    final var postingUrl = entity.getPostingUrl();
    return new JobApplication(entity.getId(), new UserId(entity.getUserId()),
      CompanyName.of(entity.getCompany()), RoleName.of(entity.getRole()),
      Source.valueOf(entity.getSource()),
      postingUrl != null ? Url.of(postingUrl) : null,
      ApplicationStatus.valueOf(entity.getStatus()),
      entity.getDateApplied(), entity.getLastUpdated(),
      entity.getNotes() != null ? Notes.of(entity.getNotes()) : null);
  }

  @SuppressWarnings("java:S4449") // false positives
  public static JobApplicationEntity toEntity(final JobApplication domain) {
    final var entity = new JobApplicationEntity();
    entity.setId(domain.id());
    entity.setUserId(domain.userId().value());
    entity.setCompany(domain.company().value());
    entity.setRole(domain.role().value());
    entity.setSource(domain.source().name());

    final var postingUrl = domain.postingUrl();
    if (postingUrl != null) {
      entity.setPostingUrl(postingUrl.value());
    }

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
