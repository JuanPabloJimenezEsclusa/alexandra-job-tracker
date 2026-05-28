package com.jobtracker.domain.port.out;

import com.jobtracker.domain.vo.UserId;

/**
 * Port for scraping job postings from external sources.
 */
public interface JobScraperPort {
  /**
   * Scrapes a job posting from the given URL for the specified user.
   */
  RawJobData scrape(UserId userId, String url);
}
