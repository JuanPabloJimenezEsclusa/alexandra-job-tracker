package dev.jpje.jobtracker.application.usecase;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.EventPublisher;
import dev.jpje.jobtracker.domain.event.JobApplicationStatusChanged;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.inbound.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.port.outbound.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.outbound.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public class TrackJobApplicationUseCase implements TrackJobApplicationPort {
  private final SaveJobApplicationPort savePort;
  private final LoadJobApplicationPort loadPort;
  private final LoadJobPostingPort loadPostingPort;
  private final Clock clock;
  private final EventPublisher eventPublisher;

  public TrackJobApplicationUseCase(final SaveJobApplicationPort savePort,
                                    final LoadJobApplicationPort loadPort,
                                    final LoadJobPostingPort loadPostingPort,
                                    final Clock clock,
                                    final EventPublisher eventPublisher) {
    this.savePort = savePort;
    this.loadPort = loadPort;
    this.loadPostingPort = loadPostingPort;
    this.clock = clock;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public JobApplication create(final UserId userId,
                               final UUID jobPostingId,
                               @Nullable final Notes notes) {
    loadPostingPort.findById(jobPostingId)
      .orElseThrow(() -> new ResourceNotFoundException("Job posting not found"));
    final var now = clock.instant();
    final var app = new JobApplication(UUID.randomUUID(), userId, jobPostingId,
      ApplicationStatus.SAVED, now, now, notes, null);
    return savePort.save(app);
  }

  @Override
  public JobApplication updateStatus(final UserId userId,
                                      final UUID applicationId,
                                      final ApplicationStatus newStatus,
                                      @Nullable final Notes notes) {
    final var app = loadPort.findByIdAndUser(applicationId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    final var now = clock.instant();
    final var previousStatus = app.status();
    var toSave = app.withStatus(newStatus, now);
    if (notes != null) {
      toSave = toSave.withNotes(notes, now);
    }
    final var updated = savePort.save(toSave);
    eventPublisher.publish(new JobApplicationStatusChanged(
      updated.id(), updated.userId(), previousStatus, newStatus, now));
    return updated;
  }

  @Override
  public List<JobApplication> list(final UserId userId,
                                    @Nullable final ApplicationStatus status) {
    return loadPort.findByUserId(userId, status);
  }

  @Override
  public void delete(final UserId userId, final UUID applicationId) {
    loadPort.findByIdAndUser(applicationId, userId)
      .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    savePort.delete(applicationId);
  }
}
