package com.jobtracker.application.service;

import java.time.Instant;

import com.jobtracker.domain.port.in.GetAnalyticsUseCase;
import com.jobtracker.domain.port.out.LoadJobApplicationPort;
import com.jobtracker.domain.service.AnalyticsCalculator;
import com.jobtracker.domain.vo.Analytics;
import com.jobtracker.domain.vo.UserId;

public class GetAnalyticsUseCaseImpl implements GetAnalyticsUseCase {
  private final LoadJobApplicationPort loadPort;
  private final AnalyticsCalculator calculator;

  public GetAnalyticsUseCaseImpl(final LoadJobApplicationPort loadPort) {
    this.loadPort = loadPort;
    this.calculator = new AnalyticsCalculator();
  }

  @Override
  public Analytics getAnalytics(final UserId userId, final Instant since) {
    var apps = loadPort.findAllByUserId(userId);
    apps = apps.stream().filter(a -> a.dateApplied().isAfter(since)).toList();
    return calculator.calculate(apps);
  }
}
