package com.jobtracker.api.resolver;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.SubmitJobPostingUseCase;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves job submission-related GraphQL mutations.
 */
@Controller
public class ScrapeResolver {
  private final SubmitJobPostingUseCase submitUseCase;

  /**
   * Constructor.
   */
  public ScrapeResolver(final SubmitJobPostingUseCase submitUseCase) {
    this.submitUseCase = submitUseCase;
  }

  /**
   * Submits a job posting from raw data (browser extension, manual entry).
   */
  @MutationMapping
  public JobPosting submitJobPosting(@ContextValue final UserId userId,
                                     @Argument("input") final JobPostingInput input) {
    return submitUseCase.submit(userId, input.url(), input.title(), input.company(), input.description(), input.source());
  }

  /**
   * Input for the submitJobPosting mutation.
   */
  public record JobPostingInput(
    String url,
    String title,
    String company,
    String description,
    Source source) {
  }
}
