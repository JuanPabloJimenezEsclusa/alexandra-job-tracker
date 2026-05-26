package com.jobtracker.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtracker.domain.model.JobApplication;
import com.jobtracker.domain.port.out.LoadJobApplicationPort;
import com.jobtracker.domain.port.out.SaveJobApplicationPort;
import com.jobtracker.domain.vo.ApplicationStatus;
import com.jobtracker.domain.vo.Source;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.persistence.mapper.JobApplicationMapper;
import com.jobtracker.persistence.repository.JobApplicationJpaRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public class JobApplicationPersistenceAdapter implements SaveJobApplicationPort, LoadJobApplicationPort {
  private final JobApplicationJpaRepository repository;
  private final JobApplicationMapper mapper = new JobApplicationMapper();

  public JobApplicationPersistenceAdapter(JobApplicationJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public void save(JobApplication application) {
    repository.save(mapper.toEntity(application));
  }

  @Override
  public void delete(UUID id) {
    repository.deleteById(id);
  }

  @Override
  public Optional<JobApplication> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<JobApplication> findByUserId(final UserId userId,
                                           @Nullable final ApplicationStatus status,
                                           @Nullable final Source source) {
    var all = repository.findByUserIdOrderByDateAppliedDesc(userId.value()).stream().map(mapper::toDomain);
      if (status != null) all = all.filter(a -> a.status() == status);
      if (source != null) all = all.filter(a -> a.source() == source);
    return all.toList();
  }

  @Override
  public List<JobApplication> findAllByUserId(UserId userId) {
    return repository.findByUserIdOrderByDateAppliedDesc(userId.value()).stream().map(mapper::toDomain).toList();
  }
}
