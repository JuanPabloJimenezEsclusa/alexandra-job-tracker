package dev.jpje.jobtracker.domain.port.in;

import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.RoleName;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public interface TrackJobApplicationPort {
  JobApplication create(UserId userId, CompanyName company, RoleName role, Source source,
                        @Nullable Url postingUrl, @Nullable Notes notes);

  JobApplication updateStatus(UUID applicationId, ApplicationStatus newStatus, @Nullable Notes notes);

  List<JobApplication> list(UserId userId, @Nullable ApplicationStatus status, @Nullable Source source);

  void delete(UUID applicationId);
}
