package dev.jpje.jobtracker.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobPostingPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.mapper.JobPostingMapper;
import dev.jpje.jobtracker.persistence.repository.JobPostingJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class JobPostingPersistenceAdapter implements SaveJobPostingPort, LoadJobPostingPort {
  private final JobPostingJpaRepository repository;

  public JobPostingPersistenceAdapter(final JobPostingJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void save(final JobPosting posting) {
    repository.save(JobPostingMapper.toEntity(posting));
  }

  @Override
  public Optional<JobPosting> findById(final UUID id) {
    return repository.findById(id).map(JobPostingMapper::toDomain);
  }

  @Override
  public List<JobPosting> findByUserId(final UserId userId) {
    return repository.findByUserId(userId.value())
      .stream()
      .map(JobPostingMapper::toDomain)
      .toList();
  }
}
