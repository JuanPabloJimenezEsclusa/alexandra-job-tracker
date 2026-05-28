package com.jobtracker.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.out.LoadJobPostingPort;
import com.jobtracker.domain.port.out.SaveJobPostingPort;
import com.jobtracker.domain.vo.UserId;
import com.jobtracker.persistence.mapper.JobPostingMapper;
import com.jobtracker.persistence.repository.JobPostingJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA adapter for job posting persistence.
 */
@Component
@Transactional(readOnly = true)
public class JobPostingPersistenceAdapter implements SaveJobPostingPort, LoadJobPostingPort {
  private final JobPostingJpaRepository repository;
  private final JobPostingMapper mapper = new JobPostingMapper();

  /**
   * Creates an adapter backed by the given JPA repository.
   */
  public JobPostingPersistenceAdapter(final JobPostingJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void save(final JobPosting posting) {
    repository.save(mapper.toEntity(posting));
  }

  @Override
  public Optional<JobPosting> findById(final UUID id) {
    return repository.findById(id).map(mapper::toDomain);
  }

  @Override
  public List<JobPosting> findByUserId(final UserId userId) {
    return repository.findByUserId(userId.value())
      .stream()
      .map(mapper::toDomain)
      .toList();
  }
}
