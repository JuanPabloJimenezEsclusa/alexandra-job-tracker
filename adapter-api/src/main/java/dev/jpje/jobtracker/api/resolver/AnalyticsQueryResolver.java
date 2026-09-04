package dev.jpje.jobtracker.api.resolver;

import java.time.Instant;
import java.util.Objects;

import dev.jpje.jobtracker.api.dto.AnalyticsResponse;
import dev.jpje.jobtracker.domain.port.inbound.GetAnalyticsPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AnalyticsQueryResolver {
  private final GetAnalyticsPort useCase;

  public AnalyticsQueryResolver(final GetAnalyticsPort useCase) {
    this.useCase = useCase;
  }

  @QueryMapping
  public AnalyticsResponse analytics(@ContextValue(required = false) @Nullable final UserId userId,
                                      @Argument @Nullable final Instant since) {
    Objects.requireNonNull(userId, "Authentication required");
    return AnalyticsResponse.from(useCase.getAnalytics(userId, since));
  }
}
