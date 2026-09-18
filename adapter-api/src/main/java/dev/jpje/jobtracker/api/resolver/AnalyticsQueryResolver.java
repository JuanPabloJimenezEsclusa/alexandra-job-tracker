package dev.jpje.jobtracker.api.resolver;

import java.time.Instant;

import dev.jpje.jobtracker.api.dto.AnalyticsResponse;
import dev.jpje.jobtracker.domain.port.inbound.GetAnalyticsPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Controller
public class AnalyticsQueryResolver {
  private final GetAnalyticsPort useCase;

  public AnalyticsQueryResolver(final GetAnalyticsPort useCase) {
    this.useCase = useCase;
  }

  @QueryMapping
  @PreAuthorize("@authz.requireUser(authentication)")
  public AnalyticsResponse analytics(@AuthenticationPrincipal final UserId userId,
                                      @Argument @Nullable final Instant since) {
    return AnalyticsResponse.from(useCase.getAnalytics(userId, since));
  }
}
