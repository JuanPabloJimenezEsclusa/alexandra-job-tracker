package com.jobtracker.api.resolver;

import java.util.UUID;

import com.jobtracker.domain.model.JobAnalysis;
import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.AnalyzeJobPostingUseCase;
import com.jobtracker.domain.port.in.SubmitJobPostingUseCase;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves job posting-related GraphQL mutations.
 */
@Controller
public class JobPostingMutationResolver {
  private final SubmitJobPostingUseCase submitUseCase;
  private final AnalyzeJobPostingUseCase analyzeUseCase;

  /**
   * Constructor.
   */
  public JobPostingMutationResolver(final SubmitJobPostingUseCase submitUseCase,
                                     final AnalyzeJobPostingUseCase analyzeUseCase) {
    this.submitUseCase = submitUseCase;
    this.analyzeUseCase = analyzeUseCase;
  }

  /**
   * Submits a job posting from raw data (browser extension, manual entry).
   */
  @SuppressWarnings("java:S4449") // false positives
  @MutationMapping
  public JobPosting submitJobPosting(@ContextValue final UserId userId,
                                      @Argument("input") final JobPostingInput input) {
    return submitUseCase.submit(userId, input.url(), input.title(), input.company(), input.description(), input.source());
  }

  /**
   * Triggers AI analysis of a job posting.
   */
  @MutationMapping
  public JobAnalysis analyzeJobPosting(@Argument final UUID jobPostingId) {
    return analyzeUseCase.analyze(jobPostingId);
  }

  /**
   * Input for the submitJobPosting mutation.
   */
  public record JobPostingInput(
    String url,
    String title,
    String company,
    @Nullable String description,
    Source source) {
  }
}
