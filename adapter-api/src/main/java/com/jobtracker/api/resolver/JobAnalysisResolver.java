package com.jobtracker.api.resolver;

import java.util.UUID;

import com.jobtracker.domain.model.JobAnalysis;
import com.jobtracker.domain.port.in.AnalyzeJobPostingUseCase;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves job analysis-related GraphQL mutations.
 */
@Controller
public class JobAnalysisResolver {
  private final AnalyzeJobPostingUseCase useCase;

  /**
   * Constructor.
   */
  public JobAnalysisResolver(final AnalyzeJobPostingUseCase useCase) {
    this.useCase = useCase;
  }

  /**
   * Triggers AI analysis of a job posting.
   */
  @MutationMapping
  public JobAnalysis analyzeJobPosting(@Argument final UUID jobPostingId) {
    return useCase.analyze(jobPostingId);
  }
}
