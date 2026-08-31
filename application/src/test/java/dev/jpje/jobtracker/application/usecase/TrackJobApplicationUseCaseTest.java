package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
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
    // Given
    final var userId = UserId.generate();
    final var postingId = UUID.randomUUID();
    when(loadPostingPort.findById(postingId)).thenReturn(Optional.of(jobPosting(postingId, userId)));
    when(clock.instant()).thenReturn(NOW);
    when(savePort.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

    // When
    final var result = useCase.create(userId, postingId, null);

    // Then
    assertThat(result)
      .extracting(JobApplication::userId, JobApplication::jobPostingId, JobApplication::status,
        JobApplication::dateApplied, JobApplication::lastUpdated)
      .containsExactly(userId, postingId, ApplicationStatus.SAVED, NOW, NOW);
    verify(savePort).save(result);
    verifyNoMoreInteractions(loadPostingPort, savePort, clock);
  }

  @Test
  void shouldThrowWhenPostingMissing() {
    // Given
    final var userId = UserId.generate();
    final var postingId = UUID.randomUUID();
    when(loadPostingPort.findById(postingId)).thenReturn(Optional.empty());

    // When, then
    assertThatThrownBy(() -> useCase.create(userId, postingId, null))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("Job posting not found");
    verify(loadPostingPort).findById(postingId);
    verifyNoMoreInteractions(savePort, clock);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("updateScenarios")
  void shouldUpdateStatus(final Notes notes, final Notes expectedNotes) {
    // Given
    final var app = application();
    when(loadPort.findById(app.id())).thenReturn(Optional.of(app));
    when(clock.instant()).thenReturn(NOW);
    when(savePort.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

    // When
    final var result = useCase.updateStatus(app.id(), ApplicationStatus.APPLIED, notes);

    // Then
    assertThat(result)
      .extracting(JobApplication::status, JobApplication::lastUpdated, JobApplication::notes)
      .containsExactly(ApplicationStatus.APPLIED, NOW, expectedNotes);
    verify(savePort).save(result);
    verify(eventPublisher).publish(new JobApplicationStatusChanged(
      app.id(), app.userId(), ApplicationStatus.SAVED, ApplicationStatus.APPLIED, NOW));
    verifyNoMoreInteractions(loadPort, savePort, clock, eventPublisher);
  }

  @Test
  void shouldThrowWhenApplicationMissing() {
    // Given
    final var id = UUID.randomUUID();
    when(loadPort.findById(id)).thenReturn(Optional.empty());

    // When, then
    assertThatThrownBy(() -> useCase.updateStatus(id, ApplicationStatus.APPLIED, null))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("Application not found");
    verify(loadPort).findById(id);
    verifyNoMoreInteractions(savePort, clock, eventPublisher);
  }

  @Test
  void shouldListApplications() {
    // Given
    final var userId = UserId.generate();
    final var apps = List.of(application());
    when(loadPort.findByUserId(userId, null)).thenReturn(apps);

    // When, then
    assertThat(useCase.list(userId, null)).isEqualTo(apps);
    verify(loadPort).findByUserId(userId, null);
    verifyNoMoreInteractions(savePort, loadPostingPort, clock, eventPublisher);
  }

  @Test
  void shouldDeleteApplication() {
    // Given
    final var id = UUID.randomUUID();

    // When
    useCase.delete(id);

    // Then
    verify(savePort).delete(id);
    verifyNoMoreInteractions(savePort, loadPort, loadPostingPort, clock, eventPublisher);
  }

  private static JobApplication application() {
    return new JobApplication(UUID.randomUUID(), UserId.generate(), UUID.randomUUID(),
      ApplicationStatus.SAVED, NOW, NOW, null, 0L);
  }

  private static JobPosting jobPosting(final UUID postingId, final UserId userId) {
    return new JobPosting(postingId, userId,
      Url.of("https://example.com/job"),
      Source.LINKEDIN,
      JobTitle.of("Engineer"),
      CompanyName.of("Acme"),
      "desc", NOW);
  }
}
