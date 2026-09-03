package dev.jpje.jobtracker.domain.port.in;

import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public interface TrackJobApplicationPort {
  JobApplication create(UserId userId, UUID jobPostingId, @Nullable Notes notes);

  JobApplication updateStatus(UserId userId, UUID applicationId, ApplicationStatus newStatus, @Nullable Notes notes);

  List<JobApplication> list(UserId userId, @Nullable ApplicationStatus status);

  void delete(UserId userId, UUID applicationId);
}
