package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.in.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Source;
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
      .set(field(JobApplication::company), "Acme")
      .set(field(JobApplication::role), "Engineer")
      .set(field(JobApplication::source), Source.LINKEDIN)
      .set(field(JobApplication::postingUrl), "url")
      .set(field(JobApplication::notes), "notes")
      .create();

    when(useCase.create(userId, "Acme", "Engineer",
      Source.LINKEDIN, "url", "notes")).thenReturn(app);

    final var result = resolver.createApplication(userId, "Acme", "Engineer",
      Source.LINKEDIN, "url", "notes");
    assertThat(result.company()).isEqualTo("Acme");
    assertThat(result.role()).isEqualTo("Engineer");
    assertThat(result.source()).isEqualTo(Source.LINKEDIN);

    verify(useCase).create(userId, "Acme", "Engineer", Source.LINKEDIN, "url", "notes");
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
      .set(field(JobApplication::company), "Acme")
      .set(field(JobApplication::role), "Engineer")
      .set(field(JobApplication::source), Source.LINKEDIN)
      .set(field(JobApplication::postingUrl), "url")
      .set(field(JobApplication::notes), "notes")
      .create();

    when(useCase.updateStatus(id, ApplicationStatus.INTERVIEWING, "notes")).thenReturn(app);

    final var result = resolver.updateApplicationStatus(id, ApplicationStatus.INTERVIEWING, "notes");
    assertThat(result.status()).isEqualTo(ApplicationStatus.INTERVIEWING);
    assertThat(result.company()).isEqualTo("Acme");

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
