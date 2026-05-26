package com.jobtracker.application.service;

import java.time.Instant;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.ScrapeJobUseCase;
import com.jobtracker.domain.port.out.JobScraperPort;
import com.jobtracker.domain.port.out.SaveJobApplicationPort;
import com.jobtracker.domain.port.out.SaveJobPostingPort;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;

public class ScrapeJobUseCaseImpl implements ScrapeJobUseCase {
  private final JobScraperPort scraper;
  private final SaveJobPostingPort savePostingPort;
  private final SaveJobApplicationPort saveAppPort;

  public ScrapeJobUseCaseImpl(final JobScraperPort scraper,
                              final SaveJobPostingPort savePostingPort,
                              final SaveJobApplicationPort saveAppPort) {
    this.scraper = scraper;
    this.savePostingPort = savePostingPort;
    this.saveAppPort = saveAppPort;
  }

  @Override
  public JobPosting scrape(final UserId userId, final String url) {
    final var raw = scraper.scrape(userId, url);
    final var posting = new JobPosting(UUID.randomUUID(), userId, raw.url(), Source.valueOf(raw.source().toUpperCase()),
        raw.title(), raw.company(), raw.description(), Instant.now());
    savePostingPort.save(posting);

    final var app = new JobApplication(UUID.randomUUID(), userId, raw.company(), raw.title(),
        Source.valueOf(raw.source().toUpperCase()), raw.url(),
        ApplicationStatus.SAVED, Instant.now(), Instant.now(), "Auto-created from scrape");
    saveAppPort.save(app);
    return posting;
  }
}
