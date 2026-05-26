package com.jobtracker.domain.port.in;

import java.time.Instant;

import com.jobtracker.domain.vo.Analytics;
import com.jobtracker.domain.vo.UserId;

public interface GetAnalyticsUseCase {
  Analytics getAnalytics(UserId userId, Instant since);
}
