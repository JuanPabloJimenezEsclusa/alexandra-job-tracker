package com.jobtracker.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.out.LoadJobPostingPort;
import com.jobtracker.domain.port.out.SaveJobPostingPort;
import com.jobtracker.persistence.mapper.JobPostingMapper;
import com.jobtracker.persistence.repository.JobPostingJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class JobPostingPersistenceAdapter implements SaveJobPostingPort, LoadJobPostingPort {
  private final JobPostingJpaRepository repository;
  private final JobPostingMapper mapper = new JobPostingMapper();

  public JobPostingPersistenceAdapter(JobPostingJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  public void save(JobPosting posting) {
    repository.save(mapper.toEntity(posting));
  }

  @Override
  public Optional<JobPosting> findById(UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }
}
