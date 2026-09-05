package dev.jpje.jobtracker.domain.port.outbound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public interface LoadJobApplicationPort {
  Optional<JobApplication> findById(UUID id);

  Optional<JobApplication> findByIdAndUser(UUID id, UserId userId);

  List<JobApplication> findByUserId(UserId userId, @Nullable ApplicationStatus status);

  List<JobApplication> findAllByUserId(UserId userId);
}
