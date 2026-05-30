package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;

import com.jobtracker.domain.port.in.GetAnalyticsUseCase;
import com.jobtracker.domain.vo.Analytics;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsQueryResolverTest {

  @InjectMocks
  private AnalyticsQueryResolver resolver;

  @Mock
  private GetAnalyticsUseCase useCase;

  @Test
  void shouldReturnAnalytics() {
    final var userId = new UserId(UUID.randomUUID());
    final var analytics = new Analytics(Map.of(ApplicationStatus.SAVED, 3));

    when(useCase.getAnalytics(userId, null)).thenReturn(analytics);

    assertThat(resolver.analytics(userId, null))
      .isNotNull()
      .extracting(Analytics::totalApplications, Analytics::conversionRate)
      .containsExactly(3, 0.0);

    verify(useCase).getAnalytics(userId, null);
    verifyNoMoreInteractions(useCase);
  }

  @Test
  void shouldMapPerStatus() {
    final var perStatus = Map.of(
      ApplicationStatus.SAVED, 5,
      ApplicationStatus.APPLIED, 3
    );
    final var analytics = new Analytics(perStatus);

    final var result = resolver.perStatus(analytics);

    assertThat(result.saved()).isEqualTo(5);
    assertThat(result.applied()).isEqualTo(3);
  }
}
