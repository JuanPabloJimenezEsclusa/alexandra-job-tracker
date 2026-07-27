package dev.jpje.jobtracker.api.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
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
      .set(field(JobApplication::company), "Acme")
      .set(field(JobApplication::role), "Engineer")
      .set(field(JobApplication::source), Source.LINKEDIN)
      .set(field(JobApplication::postingUrl), "url")
      .set(field(JobApplication::notes), "notes")
      .create();
    final var input = List.of(app);

    when(useCase.list(userId, null, null)).thenReturn(input);

    final var result = resolver.applications(userId, null, null);
    assertThat(result).hasSize(1);
    assertThat(result.getFirst().company()).isEqualTo("Acme");
    assertThat(result.getFirst().role()).isEqualTo("Engineer");

    verify(useCase).list(userId, null, null);
    verifyNoMoreInteractions(useCase);
  }
}
