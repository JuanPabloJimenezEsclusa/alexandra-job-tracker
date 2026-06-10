package com.jobtracker.application.service;

import java.time.Clock;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.SubmitJobPostingUseCase;
import com.jobtracker.domain.port.out.SaveJobApplicationPort;
import com.jobtracker.domain.port.out.SaveJobPostingPort;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;

/**
 * Implementation of SubmitJobPostingUseCase that persists raw job data directly.
 */
public class SubmitJobPostingUseCaseImpl implements SubmitJobPostingUseCase {
  private final SaveJobPostingPort savePostingPort;
  private final SaveJobApplicationPort saveAppPort;
  private final Clock clock;

  /**
   * Constructor.
   */
  public SubmitJobPostingUseCaseImpl(final SaveJobPostingPort savePostingPort,
                                      final SaveJobApplicationPort saveAppPort,
                                      final Clock clock) {
    this.savePostingPort = savePostingPort;
    this.saveAppPort = saveAppPort;
    this.clock = clock;
  }

  @Override
  public JobPosting submit(final UserId userId, final String url, final String title, final String company,
                           final String description, final Source source) {
    final var now = clock.instant();
    final var posting = new JobPosting(UUID.randomUUID(), userId, url, source,
      title, company, description, now);
    savePostingPort.save(posting);

    final var app = new JobApplication(UUID.randomUUID(), userId, company, title,
      source, url, ApplicationStatus.SAVED, now, now, "From browser extension");
    saveAppPort.save(app);
    return posting;
  }
}
