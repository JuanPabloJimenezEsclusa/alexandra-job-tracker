package com.jobtracker.api.resolver;

import java.util.List;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.port.in.TrackJobApplicationUseCase;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves job application-related GraphQL queries and mutations.
 */
@Controller
public class ApplicationResolver {
  private final TrackJobApplicationUseCase useCase;

  /**
   * Constructor.
   */
  public ApplicationResolver(final TrackJobApplicationUseCase useCase) {
    this.useCase = useCase;
  }

  /**
   * Lists job applications for the authenticated user, optionally filtered.
   */
  @QueryMapping
  public List<JobApplication> applications(@ContextValue final UserId userId,
                                           @Argument @Nullable final ApplicationStatus status,
                                           @Argument @Nullable final Source source) {
    return useCase.list(userId, status, source);
  }

  /**
   * Creates a new job application for the authenticated user.
   */
  @MutationMapping
  public JobApplication createApplication(@ContextValue final UserId userId,
                                          @Argument final String company,
                                          @Argument final String role,
                                          @Argument final Source source,
                                          @Argument @Nullable final String postingUrl,
                                          @Argument @Nullable final String notes) {
    return useCase.create(userId, company, role, source, postingUrl, notes);
  }

  /**
   * Updates the status of an existing job application.
   */
  @MutationMapping
  public JobApplication updateApplicationStatus(@Argument final UUID id,
                                                @Argument final ApplicationStatus status,
                                                @Argument @Nullable final String notes) {
    return useCase.updateStatus(id, status, notes);
  }

  /**
   * Deletes a job application by ID.
   */
  @MutationMapping
  public boolean deleteApplication(@Argument final UUID id) {
    useCase.delete(id);
    return true;
  }
}
