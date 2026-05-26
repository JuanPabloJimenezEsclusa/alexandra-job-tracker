package com.jobtracker.domain.port.in;

import java.util.List;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;

public interface TrackJobApplicationUseCase {
  JobApplication create(UserId userId, String company, String role, Source source, String postingUrl, String notes);

  JobApplication updateStatus(UUID applicationId, ApplicationStatus newStatus, String notes);

  List<JobApplication> list(UserId userId, ApplicationStatus status, Source source);

  void delete(UUID applicationId);
}
