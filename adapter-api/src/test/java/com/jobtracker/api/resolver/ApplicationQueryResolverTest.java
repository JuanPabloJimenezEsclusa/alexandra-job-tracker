package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.port.in.TrackJobApplicationUseCase;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationQueryResolverTest {

  @InjectMocks
  private ApplicationQueryResolver resolver;

  @Mock
  private TrackJobApplicationUseCase useCase;

  @Test
  void shouldListApplications() {
    final var userId = new UserId(UUID.randomUUID());
    final var app = new JobApplication(UUID.randomUUID(), userId, "Acme", "Engineer",
      Source.LINKEDIN, "url", ApplicationStatus.SAVED, Instant.now(), Instant.now(), "notes");
    final var expected = List.of(app);

    when(useCase.list(userId, null, null)).thenReturn(expected);

    assertThat(resolver.applications(userId, null, null)).isEqualTo(expected);

    verify(useCase).list(userId, null, null);
    verifyNoMoreInteractions(useCase);
  }
}
