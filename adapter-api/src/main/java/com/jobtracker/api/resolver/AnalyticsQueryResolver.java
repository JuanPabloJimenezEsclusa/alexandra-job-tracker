package com.jobtracker.api.resolver;

import java.time.Instant;
import java.util.Objects;

import com.jobtracker.api.dto.AnalyticsResponse;
import com.jobtracker.domain.port.in.GetAnalyticsUseCase;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * Resolves analytics-related GraphQL queries.
 */
@Controller
public class AnalyticsQueryResolver {
  private final GetAnalyticsUseCase useCase;

  /**
   * Constructor.
   */
  public AnalyticsQueryResolver(final GetAnalyticsUseCase useCase) {
    this.useCase = useCase;
  }

  /**
   * Returns analytics for the authenticated user, optionally filtered by date.
   */
  @QueryMapping
  public AnalyticsResponse analytics(@ContextValue(required = false) @Nullable final UserId userId,
                                      @Argument @Nullable final Instant since) {
    Objects.requireNonNull(userId, "Authentication required");
    return AnalyticsResponse.from(useCase.getAnalytics(userId, since));
  }
}
