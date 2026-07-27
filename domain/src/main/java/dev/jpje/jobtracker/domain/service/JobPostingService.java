package dev.jpje.jobtracker.domain.service;

import java.time.Instant;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.EventPublisher;
import dev.jpje.jobtracker.domain.event.JobPostingSubmitted;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.RoleName;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;

public class JobPostingService {
  private final EventPublisher eventPublisher;

  public JobPostingService(final EventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  public record SubmittedPosting(JobPosting posting, JobApplication tracking) {
  }

  public SubmittedPosting submit(final UserId userId, final Url url, final JobTitle title,
                                 final CompanyName company, final String description,
                                 final Source source, final Instant now) {
    final var posting = new JobPosting(UUID.randomUUID(), userId, url, source,
      title, company, description, now);
    final var tracking = new JobApplication(UUID.randomUUID(), userId, company,
      RoleName.of(title.value()), source, url, ApplicationStatus.SAVED, now, now, null);
    eventPublisher.publish(new JobPostingSubmitted(posting.id(), userId, now));
    return new SubmittedPosting(posting, tracking);
  }
}
