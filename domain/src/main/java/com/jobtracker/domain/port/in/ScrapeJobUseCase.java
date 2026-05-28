package com.jobtracker.domain.port.in;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.vo.UserId;

/**
 * Use case for scraping job postings.
 */
public interface ScrapeJobUseCase {
  /**
   * Scrapes a job posting for a user.
   */
  JobPosting scrape(UserId userId, String url);
}
