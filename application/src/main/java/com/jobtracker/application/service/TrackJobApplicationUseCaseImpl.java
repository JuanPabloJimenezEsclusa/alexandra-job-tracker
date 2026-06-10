package com.jobtracker.application.service;

import java.time.Clock;
import java.util.List;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.port.in.TrackJobApplicationUseCase;
import com.jobtracker.domain.port.out.LoadJobApplicationPort;
import com.jobtracker.domain.port.out.SaveJobApplicationPort;
import com.jobtracker.domain.service.ApplicationTrackerService;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

/**
 * Implementation of TrackJobApplicationUseCase with create, update, list, and delete operations.
 */
public class TrackJobApplicationUseCaseImpl implements TrackJobApplicationUseCase {
  private final SaveJobApplicationPort savePort;
  private final LoadJobApplicationPort loadPort;
  private final ApplicationTrackerService tracker;
  private final Clock clock;

  /**
   * Constructor.
   */
  public TrackJobApplicationUseCaseImpl(final SaveJobApplicationPort savePort,
                                        final LoadJobApplicationPort loadPort,
                                        final Clock clock) {
    this.savePort = savePort;
    this.loadPort = loadPort;
    this.clock = clock;
    this.tracker = new ApplicationTrackerService();
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
