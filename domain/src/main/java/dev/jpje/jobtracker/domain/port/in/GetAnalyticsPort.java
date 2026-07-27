package dev.jpje.jobtracker.domain.port.in;

import java.time.Instant;

import dev.jpje.jobtracker.domain.vo.Analytics;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public interface GetAnalyticsPort {
  Analytics getAnalytics(UserId userId, @Nullable Instant since);
}
