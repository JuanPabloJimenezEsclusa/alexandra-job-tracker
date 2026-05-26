package com.jobtracker.domain.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public interface LoadJobApplicationPort {
  Optional<JobApplication> findById(UUID id);
  List<JobApplication> findByUserId(UserId userId, @Nullable ApplicationStatus status, @Nullable Source source);
  List<JobApplication> findAllByUserId(UserId userId);
}
