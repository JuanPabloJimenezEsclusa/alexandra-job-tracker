package dev.jpje.jobtracker.application.usecase;

import java.time.Instant;

import dev.jpje.jobtracker.domain.port.in.GetAnalyticsPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.service.AnalyticsCalculator;
import dev.jpje.jobtracker.domain.vo.Analytics;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public class GetAnalyticsUseCase implements GetAnalyticsPort {
  private final LoadJobApplicationPort loadPort;
  private final AnalyticsCalculator calculator;

  public GetAnalyticsUseCase(final LoadJobApplicationPort loadPort,
                             final AnalyticsCalculator calculator) {
    this.loadPort = loadPort;
    this.calculator = calculator;
  }

  @Override
  public Analytics getAnalytics(final UserId userId, @Nullable final Instant since) {
    var apps = loadPort.findAllByUserId(userId);
    apps = apps.stream().filter(a -> since == null || a.dateApplied().isAfter(since)).toList();
    return calculator.calculate(apps);
  }
}
