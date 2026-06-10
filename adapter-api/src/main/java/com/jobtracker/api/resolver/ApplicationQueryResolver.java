package com.jobtracker.api.resolver;

import java.util.List;
import java.util.Objects;

import com.jobtracker.api.dto.JobApplicationResponse;
import com.jobtracker.domain.port.in.TrackJobApplicationUseCase;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves job application-related GraphQL queries.
 */
@Controller
public class ApplicationQueryResolver {
  private final TrackJobApplicationUseCase useCase;

  /**
   * Constructor.
   */
  public ApplicationQueryResolver(final TrackJobApplicationUseCase useCase) {
    this.useCase = useCase;
  }

  /**
   * Lists job applications for the authenticated user, optionally filtered.
   */
  @QueryMapping
  public List<JobApplicationResponse> applications(@ContextValue(required = false) @Nullable final UserId userId,
                                                    @Argument @Nullable final ApplicationStatus status,
                                                    @Argument @Nullable final Source source) {
    Objects.requireNonNull(userId, "Authentication required");
    return useCase.list(userId, status, source).stream()
      .map(JobApplicationResponse::from)
      .toList();
  }
}
