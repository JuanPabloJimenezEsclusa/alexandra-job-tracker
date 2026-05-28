package com.jobtracker.domain.port.in;

import java.time.Instant;

import com.jobtracker.domain.vo.Analytics;
import com.jobtracker.domain.vo.UserId;

/**
 * Use case for retrieving analytics about job applications.
 */
public interface GetAnalyticsUseCase {
  /**
   * Returns analytics for the given user since the specified time.
   */
  Analytics getAnalytics(UserId userId, Instant since);
}
