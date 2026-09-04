package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobApplicationResponse;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.inbound.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
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
  private TrackJobApplicationPort useCase;

  @Test
  void shouldCreateApplication() {
    final var userId = new UserId(UUID.randomUUID());
    final var app = Instancio.of(JobApplication.class)
      .set(field(JobApplication::userId), userId)
      .set(field(JobApplication::status), ApplicationStatus.SAVED)
      .set(field(JobApplication::notes), Notes.of("notes"))
      .create();

    when(useCase.create(userId, app.jobPostingId(), Notes.of("notes"))).thenReturn(app);

    final var result = resolver.createApplication(userId, app.jobPostingId(), "notes");
    assertThat(result)
      .as("created application should reference the posting")
      .extracting(JobApplicationResponse::jobPostingId, JobApplicationResponse::status)
      .containsExactly(app.jobPostingId(), ApplicationStatus.SAVED);

    verify(useCase, description("application should be created once"))
      .create(userId, app.jobPostingId(), Notes.of("notes"));
    verifyNoMoreInteractions(useCase);
  }

  @Test
  void shouldUpdateApplicationStatus() {
    final var id = UUID.randomUUID();
    final var userId = new UserId(id);
    final var app = Instancio.of(JobApplication.class)
      .set(field(JobApplication::id), id)
      .set(field(JobApplication::userId), userId)
      .set(field(JobApplication::status), ApplicationStatus.INTERVIEWING)
      .set(field(JobApplication::notes), Notes.of("notes"))
      .create();

    when(useCase.updateStatus(userId, id, ApplicationStatus.INTERVIEWING, Notes.of("notes"))).thenReturn(app);

    final var result = resolver.updateApplicationStatus(userId, id, ApplicationStatus.INTERVIEWING, "notes");
    assertThat(result)
      .as("updated application should reflect the new status")
      .extracting(JobApplicationResponse::status)
      .isEqualTo(ApplicationStatus.INTERVIEWING);

    verify(useCase, description("application status should be updated once"))
      .updateStatus(userId, id, ApplicationStatus.INTERVIEWING, Notes.of("notes"));
    verifyNoMoreInteractions(useCase);
  }

  @Test
  void shouldRejectUpdateWithoutAuthentication() {
    final var id = UUID.randomUUID();

    assertThatThrownBy(() -> resolver.updateApplicationStatus(null, id, ApplicationStatus.APPLIED, null))
      .as("update without auth should fail")
      .isInstanceOf(NullPointerException.class)
      .hasMessage("Authentication required");
    verifyNoMoreInteractions(useCase);
  }

  @Test
  void shouldDeleteApplication() {
    final var id = UUID.randomUUID();
    final var userId = new UserId(UUID.randomUUID());

    assertThat(resolver.deleteApplication(userId, id)).as("deletion should succeed").isTrue();

    verify(useCase, description("application deletion should be delegated")).delete(userId, id);
    verifyNoMoreInteractions(useCase);
  }

  @Test
  void shouldRejectDeleteWithoutAuthentication() {
    final var id = UUID.randomUUID();

    assertThatThrownBy(() -> resolver.deleteApplication(null, id))
      .as("delete without auth should fail")
      .isInstanceOf(NullPointerException.class)
      .hasMessage("Authentication required");
    verifyNoMoreInteractions(useCase);
  }
}
