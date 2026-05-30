package com.jobtracker.api.resolver;

import java.util.List;

import com.jobtracker.application.service.ListJobPostingsUseCaseImpl;
import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.in.ListJobPostingsUseCase;
import com.jobtracker.domain.port.out.LoadJobPostingPort;
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
public class JobPostingResolver {
  private final ListJobPostingsUseCase useCase;

  /**
   * Constructor.
   */
  public JobPostingResolver(final LoadJobPostingPort loadPort) {
    this.useCase = new ListJobPostingsUseCaseImpl(loadPort);
  }

  /**
   * Lists job postings for the authenticated user, optionally filtered by source.
   */
  @QueryMapping
  public List<JobPosting> jobPostings(@ContextValue final UserId userId,
                                      @Argument @Nullable final Source source) {
    return useCase.listJobPostings(userId, source);
  }
}
