package com.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
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
class ApplicationMutationResolverTest {

  @InjectMocks
  private ApplicationMutationResolver resolver;

  @Mock
  private TrackJobApplicationUseCase useCase;

  @Test
  void shouldCreateApplication() {
    final var userId = new UserId(UUID.randomUUID());
    final var app = new JobApplication(UUID.randomUUID(), userId, "Acme", "Engineer",
      Source.LINKEDIN, "url", ApplicationStatus.SAVED, Instant.now(), Instant.now(), "notes");

    when(useCase.create(userId, "Acme", "Engineer",
      Source.LINKEDIN, "url", "notes")).thenReturn(app);

    assertThat(resolver.createApplication(userId, "Acme", "Engineer",
      Source.LINKEDIN, "url", "notes")).isEqualTo(app);

    verify(useCase).create(userId, "Acme", "Engineer", Source.LINKEDIN, "url", "notes");
    verifyNoMoreInteractions(useCase);
  }

  @Test
  void shouldUpdateApplicationStatus() {
    final var id = UUID.randomUUID();
    final var app = new JobApplication(id, new UserId(id), "Acme", "Engineer",
      Source.LINKEDIN, "url", ApplicationStatus.INTERVIEWING, Instant.now(), Instant.now(), "notes");

    when(useCase.updateStatus(id, ApplicationStatus.INTERVIEWING, "notes")).thenReturn(app);

    assertThat(resolver.updateApplicationStatus(id, ApplicationStatus.INTERVIEWING, "notes")).isEqualTo(app);

    verify(useCase).updateStatus(id, ApplicationStatus.INTERVIEWING, "notes");
    verifyNoMoreInteractions(useCase);
  }

  @Test
  void shouldDeleteApplication() {
    final var id = UUID.randomUUID();

    assertThat(resolver.deleteApplication(id)).isTrue();

    verify(useCase).delete(id);
    verifyNoMoreInteractions(useCase);
  }
}
