package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import dev.jpje.jobtracker.api.dto.AnalyticsResponse;
import dev.jpje.jobtracker.domain.port.in.GetAnalyticsPort;
import dev.jpje.jobtracker.domain.vo.Analytics;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsQueryResolverTest {

  private static final int EXPECTED_TOTAL = 3;
  private static final double EXPECTED_CONVERSION = 0.0;
  private static final int EXPECTED_SAVED = 3;

  @InjectMocks
  private AnalyticsQueryResolver resolver;

  @Mock
  private GetAnalyticsPort useCase;

  @Test
  void shouldReturnAnalytics() {
    final var userId = new UserId(UUID.randomUUID());
    final var analytics = new Analytics(Map.of(ApplicationStatus.SAVED, EXPECTED_TOTAL));

    when(useCase.getAnalytics(userId, null)).thenReturn(analytics);

    final var result = resolver.analytics(userId, null);
    assertThat(result)
      .as("analytics should expose totals, conversion and per-status counts")
      .extracting(AnalyticsResponse::totalApplications, AnalyticsResponse::conversionRate,
        r -> r.perStatus().saved())
      .containsExactly(EXPECTED_TOTAL, EXPECTED_CONVERSION, EXPECTED_SAVED);

    verify(useCase, description("analytics should be fetched once")).getAnalytics(userId, null);
    verifyNoMoreInteractions(useCase);
  }
}
