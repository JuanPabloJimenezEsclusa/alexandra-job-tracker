package dev.jpje.jobtracker.application.usecase;

import java.time.Clock;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.in.SubmitJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobPostingPort;
import dev.jpje.jobtracker.domain.service.JobPostingService;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;

public class SubmitJobPostingUseCase implements SubmitJobPostingPort {
  private final SaveJobPostingPort savePostingPort;
  private final SaveJobApplicationPort saveAppPort;
  private final JobPostingService jobPostingService;
  private final Clock clock;

  public SubmitJobPostingUseCase(final SaveJobPostingPort savePostingPort,
                                 final SaveJobApplicationPort saveAppPort,
                                 final JobPostingService jobPostingService,
                                 final Clock clock) {
    this.savePostingPort = savePostingPort;
    this.saveAppPort = saveAppPort;
    this.jobPostingService = jobPostingService;
    this.clock = clock;
  }

  @Override
  public JobPosting submit(final UserId userId, final Url url, final JobTitle title,
                           final CompanyName company, final String description, final Source source) {
    final var result = jobPostingService.submit(
      userId, url, title, company, description, source, clock.instant());
    savePostingPort.save(result.posting());
    saveAppPort.save(result.tracking());
    return result.posting();
  }
}
