package com.jobtracker.api.resolver;

import java.time.Instant;

import com.jobtracker.api.dto.StatusCounts;
import com.jobtracker.application.service.GetAnalyticsUseCaseImpl;
import com.jobtracker.domain.port.out.LoadJobApplicationPort;
import com.jobtracker.domain.vo.Analytics;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves analytics-related GraphQL queries.
 */
@Controller
public class AnalyticsResolver {
  private final GetAnalyticsUseCaseImpl useCase;

  /**
   * Constructor.
   */
  public AnalyticsResolver(final LoadJobApplicationPort loadPort) {
    this.useCase = new GetAnalyticsUseCaseImpl(loadPort);
  }

  /**
   * Returns analytics for the authenticated user, optionally filtered by date.
   */
  @QueryMapping
  public Analytics analytics(@ContextValue final UserId userId, @Argument @Nullable final Instant since) {
    return useCase.getAnalytics(userId, since);
  }

  /**
   * Maps analytics status counts to the API response DTO.
   */
  @SchemaMapping
  public StatusCounts perStatus(final Analytics analytics) {
    return StatusCounts.fromPerStatus(analytics.perStatus());
  }
}
