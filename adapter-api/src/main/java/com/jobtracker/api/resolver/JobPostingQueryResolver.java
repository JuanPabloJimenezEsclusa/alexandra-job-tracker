package com.jobtracker.api.resolver;

import java.util.List;

import java.util.Objects;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.ListJobPostingsUseCase;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves job posting-related GraphQL queries.
 */
@Controller
public class JobPostingQueryResolver {
  private final ListJobPostingsUseCase useCase;

  /**
   * Constructor.
   */
  public JobPostingQueryResolver(final ListJobPostingsUseCase useCase) {
    this.useCase = useCase;
  }

  /**
   * Lists job postings for the authenticated user, optionally filtered by source.
   */
  @QueryMapping
  public List<JobPosting> jobPostings(@ContextValue(required = false) @Nullable final UserId userId,
                                       @Argument @Nullable final Source source) {
    Objects.requireNonNull(userId, "Authentication required");
    return useCase.listJobPostings(userId, source);
  }
}
