package dev.jpje.jobtracker.application.usecase;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.in.TrackJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.service.ApplicationTracker;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public class TrackJobApplicationUseCase implements TrackJobApplicationPort {
  private final SaveJobApplicationPort savePort;
  private final LoadJobApplicationPort loadPort;
  private final ApplicationTracker tracker;
  private final Clock clock;

  public TrackJobApplicationUseCase(final SaveJobApplicationPort savePort,
                                    final LoadJobApplicationPort loadPort,
                                    final Clock clock) {
    this.savePort = savePort;
    this.loadPort = loadPort;
    this.clock = clock;
    this.tracker = new ApplicationTracker();
  }

  @Override
  public JobApplication create(final UserId userId,
                               final String company,
                               final String role,
                               final Source source,
                               @Nullable final String postingUrl,
                               @Nullable final String notes) {
    final var now = clock.instant();
    final var app = new JobApplication(UUID.randomUUID(), userId, company, role, source, postingUrl,
      ApplicationStatus.SAVED, now, now, notes);
    savePort.save(app);
    return app;
  }

  @Override
  public JobApplication updateStatus(final UUID applicationId,
                                     final ApplicationStatus newStatus,
                                     @Nullable final String notes) {
    final var app = loadPort.findById(applicationId)
      .orElseThrow(() -> new IllegalArgumentException("Application not found"));
    final var now = clock.instant();
    var updated = tracker.transitionStatus(app, newStatus, now);
    updated = updated.withNotes(notes, now);
    savePort.save(updated);
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
