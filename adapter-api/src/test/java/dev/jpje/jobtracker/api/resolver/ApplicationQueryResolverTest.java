package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.api.dto.JobApplicationResponse;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.in.TrackJobApplicationPort;
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
class ApplicationQueryResolverTest {

  @InjectMocks
  private ApplicationQueryResolver resolver;

  @Mock
  private TrackJobApplicationPort useCase;

  @Test
  void shouldListApplications() {
    final var userId = new UserId(UUID.randomUUID());
    final var app = Instancio.of(JobApplication.class)
      .set(field(JobApplication::userId), userId)
      .set(field(JobApplication::status), ApplicationStatus.SAVED)
      .set(field(JobApplication::notes), Notes.of("notes"))
      .create();
    final var input = List.of(app);

    when(useCase.list(userId, null)).thenReturn(input);

    final var result = resolver.applications(userId, null);
    assertThat(result)
      .as("listed applications should contain the stored application")
      .singleElement()
      .extracting(JobApplicationResponse::jobPostingId, JobApplicationResponse::status)
      .containsExactly(app.jobPostingId(), ApplicationStatus.SAVED);

    verify(useCase, description("applications should be loaded once")).list(userId, null);
    verifyNoMoreInteractions(useCase);
  }
}
