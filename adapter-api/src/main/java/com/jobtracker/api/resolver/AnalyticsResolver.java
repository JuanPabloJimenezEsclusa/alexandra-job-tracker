package com.jobtracker.api.resolver;

import java.time.Instant;

import com.jobtracker.application.service.GetAnalyticsUseCaseImpl;
import com.jobtracker.domain.port.out.LoadJobApplicationPort;
import com.jobtracker.domain.vo.Analytics;
import com.jobtracker.domain.vo.UserId;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AnalyticsResolver {
  private final GetAnalyticsUseCaseImpl useCase;

  public AnalyticsResolver(final LoadJobApplicationPort loadPort) {
    this.useCase = new GetAnalyticsUseCaseImpl(loadPort);
  }

  @QueryMapping
  public Analytics analytics(@ContextValue final UserId userId, @Argument final Instant since) {
    return useCase.getAnalytics(userId, since);
  }
}
