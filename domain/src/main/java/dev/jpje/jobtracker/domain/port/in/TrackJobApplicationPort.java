package dev.jpje.jobtracker.domain.port.in;

import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public interface TrackJobApplicationPort {
  JobApplication create(UserId userId, String company, String role, Source source,
                        @Nullable String postingUrl, @Nullable String notes);

  JobApplication updateStatus(UUID applicationId, ApplicationStatus newStatus, @Nullable String notes);

  List<JobApplication> list(UserId userId, @Nullable ApplicationStatus status, @Nullable Source source);

  void delete(UUID applicationId);
}
