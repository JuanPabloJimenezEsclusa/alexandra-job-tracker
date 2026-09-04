package dev.jpje.jobtracker.application.usecase;

import java.time.Clock;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.inbound.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobPostingPort;
import dev.jpje.jobtracker.domain.service.JobPostingService;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;

public class SubmitJobPostingUseCase implements SubmitJobPostingPort {
  private final SaveJobPostingPort savePostingPort;
  private final JobPostingService jobPostingService;
  private final Clock clock;

  public SubmitJobPostingUseCase(final SaveJobPostingPort savePostingPort,
                                 final JobPostingService jobPostingService,
                                 final Clock clock) {
    this.savePostingPort = savePostingPort;
    this.jobPostingService = jobPostingService;
    this.clock = clock;
  }

  @Override
  public JobPosting submit(final UserId userId, final Url url, final JobTitle title,
                           final CompanyName company, final String description, final Source source) {
    final var posting = new JobPosting(UUID.randomUUID(), userId, url, source, title, company, description, clock.instant());
    savePostingPort.save(posting);
    jobPostingService.submit(posting);
    return posting;
  }
}
