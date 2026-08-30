package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import dev.jpje.jobtracker.domain.event.EventPublisher;
import dev.jpje.jobtracker.domain.event.JobApplicationStatusChanged;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.out.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrackJobApplicationUseCaseTest {

  private static final Instant NOW = Instant.EPOCH;

  @Mock
  private SaveJobApplicationPort savePort;

  @Mock
  private LoadJobApplicationPort loadPort;

  @Mock
  private LoadJobPostingPort loadPostingPort;

  @Mock
  private Clock clock;

  @Mock
  private EventPublisher eventPublisher;

  @InjectMocks
  private TrackJobApplicationUseCase useCase;

  private static Stream<Arguments> updateScenarios() {
    return Stream.of(
      arguments(named("without notes", null), null),
      arguments(named("with notes", Notes.of("followed up")), Notes.of("followed up"))
    );
  }

  @Test
  void shouldCreateApplicationAsSaved() {
    final var userId = UserId.generate();
    final var postingId = UUID.randomUUID();
    when(loadPostingPort.findById(postingId)).thenReturn(Optional.of(jobPosting(postingId, userId)));
    when(clock.instant()).thenReturn(NOW);
    when(savePort.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

    final var result = useCase.create(userId, postingId, null);

    assertThat(result)
      .extracting(JobApplication::userId, JobApplication::jobPostingId, JobApplication::status,
        JobApplication::dateApplied, JobApplication::lastUpdated)
      .containsExactly(userId, postingId, ApplicationStatus.SAVED, NOW, NOW);
    verify(savePort).save(result);
  }

  @Test
  void shouldThrowWhenPostingMissing() {
    final var userId = UserId.generate();
    final var postingId = UUID.randomUUID();
    when(loadPostingPort.findById(postingId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.create(userId, postingId, null))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("Job posting not found");
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("updateScenarios")
  void shouldUpdateStatus(final Notes notes, final Notes expectedNotes) {
    final var app = application();
    when(loadPort.findById(app.id())).thenReturn(Optional.of(app));
    when(clock.instant()).thenReturn(NOW);
    when(savePort.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

    final var result = useCase.updateStatus(app.id(), ApplicationStatus.APPLIED, notes);

    assertThat(result)
      .extracting(JobApplication::status, JobApplication::lastUpdated, JobApplication::notes)
      .containsExactly(ApplicationStatus.APPLIED, NOW, expectedNotes);
    verify(savePort).save(result);
    verify(eventPublisher).publish(new JobApplicationStatusChanged(
      app.id(), app.userId(), ApplicationStatus.SAVED, ApplicationStatus.APPLIED, NOW));
  }

  @Test
  void shouldThrowWhenApplicationMissing() {
    final var id = UUID.randomUUID();
    when(loadPort.findById(id)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> useCase.updateStatus(id, ApplicationStatus.APPLIED, null))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("Application not found");
  }

  @Test
  void shouldListApplications() {
    final var userId = UserId.generate();
    final var apps = List.of(application());
    when(loadPort.findByUserId(userId, null)).thenReturn(apps);

    assertThat(useCase.list(userId, null)).isEqualTo(apps);
  }

  @Test
  void shouldDeleteApplication() {
    final var id = UUID.randomUUID();

    assertThatCode(() -> useCase.delete(id)).doesNotThrowAnyException();
    verify(savePort).delete(id);
  }

  private static JobApplication application() {
    return new JobApplication(UUID.randomUUID(), UserId.generate(), UUID.randomUUID(),
      ApplicationStatus.SAVED, NOW, NOW, null, 0L);
  }

  private static JobPosting jobPosting(final UUID postingId, final UserId userId) {
    return new JobPosting(postingId, userId,
      dev.jpje.jobtracker.domain.vo.Url.of("https://example.com/job"),
      dev.jpje.jobtracker.domain.vo.Source.LINKEDIN,
      dev.jpje.jobtracker.domain.vo.JobTitle.of("Engineer"),
      dev.jpje.jobtracker.domain.vo.CompanyName.of("Acme"),
      "desc", NOW);
  }
}
