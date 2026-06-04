package com.jobtracker.api.resolver;

import java.util.Objects;
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
  @MutationMapping
  public JobPosting submitJobPosting(@ContextValue(required = false) @Nullable final UserId userId,
                                     @Argument("input") final JobPostingInput raw) {
    Objects.requireNonNull(userId, "Authentication required");
    return submitUseCase.submit(userId,
      StringSanitizer.sanitize(raw.url()),
      StringSanitizer.sanitize(raw.title()),
      StringSanitizer.sanitize(raw.company()),
      StringSanitizer.sanitize(raw.description()),
      raw.source());
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
    String description,
    Source source) {
  }
}
