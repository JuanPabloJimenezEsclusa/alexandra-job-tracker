package dev.jpje.jobtracker.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
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
    when(loadPort.findByIdAndUser(app.id(), app.userId())).thenReturn(Optional.of(app));
    when(clock.instant()).thenReturn(NOW);
    when(savePort.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

    // When
    final var result = useCase.updateStatus(app.userId(), app.id(), ApplicationStatus.APPLIED, notes);

    // Then
    assertThat(result)
      .extracting(JobApplication::status, JobApplication::lastUpdated, JobApplication::notes)
      .containsExactly(ApplicationStatus.APPLIED, NOW, expectedNotes);
    verify(savePort).save(result);
    verify(eventPublisher).publish(new JobApplicationStatusChanged(
      app.id(), app.userId(), ApplicationStatus.SAVED, ApplicationStatus.APPLIED, NOW));
    verifyNoMoreInteractions(loadPort, savePort, clock, eventPublisher);
  }

  private static Stream<Arguments> notAccessibleScenarios() {
    return Stream.of(
      arguments(named("missing application", UUID.randomUUID())),
      arguments(named("another user's application", UUID.randomUUID()))
    );
  }

  @ParameterizedTest(name = "{0} rejects the update with NOT_FOUND")
  @MethodSource("notAccessibleScenarios")
  void shouldRejectUpdateWhenApplicationNotAccessible(final UUID id) {
    // Given
    final var userId = UserId.generate();
    when(loadPort.findByIdAndUser(id, userId)).thenReturn(Optional.empty());

    // When, then
    assertThatThrownBy(() -> useCase.updateStatus(userId, id, ApplicationStatus.APPLIED, null))
      .as("an application that is missing or not owned should be indistinguishable")
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("Application not found");
    verify(loadPort, description("scoped lookup should miss for the caller")).findByIdAndUser(id, userId);
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
    final var app = application();
    when(loadPort.findByIdAndUser(app.id(), app.userId())).thenReturn(Optional.of(app));

    // When
    useCase.delete(app.userId(), app.id());

    // Then
    verify(loadPort, description("owned application lookup should hit")).findByIdAndUser(app.id(), app.userId());
    verify(savePort).delete(app.id());
    verifyNoMoreInteractions(savePort, loadPort, loadPostingPort, clock, eventPublisher);
  }

  @Test
  void shouldRejectDeleteOfAnotherUsersApplication() {
    // Given
    final var app = application();
    final var otherUser = UserId.generate();
    when(loadPort.findByIdAndUser(app.id(), otherUser)).thenReturn(Optional.empty());

    // When, then
    assertThatThrownBy(() -> useCase.delete(otherUser, app.id()))
      .isInstanceOf(ResourceNotFoundException.class)
      .hasMessage("Application not found");
    verify(loadPort, description("scoped lookup should miss for another user")).findByIdAndUser(app.id(), otherUser);
    verifyNoMoreInteractions(savePort, clock, eventPublisher);
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
