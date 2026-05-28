package dev.jpje.jobtracker.application.usecase;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.domain.event.EventPublisher;
import dev.jpje.jobtracker.domain.event.JobApplicationStatusChanged;
import dev.jpje.jobtracker.domain.exception.ResourceNotFoundException;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.in.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.RoleName;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public class TrackJobApplicationUseCase implements TrackJobApplicationPort {
  private final SaveJobApplicationPort savePort;
  private final LoadJobApplicationPort loadPort;
  private final Clock clock;
  private final EventPublisher eventPublisher;

  public TrackJobApplicationUseCase(final SaveJobApplicationPort savePort,
                                    final LoadJobApplicationPort loadPort,
                                    final Clock clock,
                                    final EventPublisher eventPublisher) {
    this.savePort = savePort;
    this.loadPort = loadPort;
    this.clock = clock;
    this.eventPublisher = eventPublisher;
  }

  @Override
  public JobApplication create(final UserId userId,
                               final CompanyName company,
                               final RoleName role,
                               final Source source,
                               @Nullable final Url postingUrl,
                               @Nullable final Notes notes) {
    final var now = clock.instant();
    final var app = new JobApplication(UUID.randomUUID(), userId, company, role, source, postingUrl,
      ApplicationStatus.SAVED, now, now, notes, null);
    return savePort.save(app);
  }

  @Override
  public JobApplication updateStatus(final UUID applicationId,
                                      final ApplicationStatus newStatus,
                                      @Nullable final Notes notes) {
    final var app = loadPort.findById(applicationId)
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
                                    @Nullable final ApplicationStatus status,
                                    @Nullable final Source source) {
    return loadPort.findByUserId(userId, status, source);
  }

  @Override
  public void delete(final UUID applicationId) {
    savePort.delete(applicationId);
  }
}
