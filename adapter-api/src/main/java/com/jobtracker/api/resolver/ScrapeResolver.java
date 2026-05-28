package com.jobtracker.api.resolver;

import com.jobtracker.application.service.ScrapeJobUseCaseImpl;
import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.out.JobScraperPort;
import com.jobtracker.domain.port.out.SaveJobApplicationPort;
import com.jobtracker.domain.port.out.SaveJobPostingPort;
import com.jobtracker.domain.vo.UserId;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves job scraping-related GraphQL mutations.
 */
@Controller
public class ScrapeResolver {
  private final ScrapeJobUseCaseImpl useCase;

  /**
   * Constructor.
   */
  public ScrapeResolver(JobScraperPort scraper, SaveJobPostingPort spp, SaveJobApplicationPort sap) {
    this.useCase = new ScrapeJobUseCaseImpl(scraper, spp, sap);
  }

  /**
   * Scrapes a job posting from the given URL for the authenticated user.
   */
  @MutationMapping
  public JobPosting scrapeJobPosting(@ContextValue UserId userId, @Argument String url) {
    return useCase.scrape(userId, url);
  }
}
