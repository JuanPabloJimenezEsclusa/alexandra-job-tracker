package com.jobtracker.application.service;

import java.time.Instant;
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

  /**
   * Constructor.
   */
  public SubmitJobPostingUseCaseImpl(final SaveJobPostingPort savePostingPort,
                                     final SaveJobApplicationPort saveAppPort) {
    this.savePostingPort = savePostingPort;
    this.saveAppPort = saveAppPort;
  }

  @Override
  public JobPosting submit(final UserId userId, final String url, final String title, final String company,
                           final String description, final Source source) {
    final var posting = new JobPosting(UUID.randomUUID(), userId, url, source,
      title, company, description, Instant.now());
    savePostingPort.save(posting);

    final var app = new JobApplication(UUID.randomUUID(), userId, company, title,
      source, url, ApplicationStatus.SAVED, Instant.now(), Instant.now(), "From browser extension");
    saveAppPort.save(app);
    return posting;
  }
}
