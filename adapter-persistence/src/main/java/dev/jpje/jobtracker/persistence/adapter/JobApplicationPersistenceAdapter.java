package dev.jpje.jobtracker.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.out.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobApplicationEntity;
import dev.jpje.jobtracker.persistence.mapper.JobApplicationMapper;
import dev.jpje.jobtracker.persistence.repository.JobApplicationJpaRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class JobApplicationPersistenceAdapter implements SaveJobApplicationPort, LoadJobApplicationPort {
  private final JobApplicationJpaRepository repository;

  public JobApplicationPersistenceAdapter(final JobApplicationJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public JobApplication save(final JobApplication application) {
    try {
      return JobApplicationMapper.toDomain(repository.saveAndFlush(JobApplicationMapper.toEntity(application)));
    } catch (final DataIntegrityViolationException e) {
      throw new ResourceAlreadyExistsException("Application already exists", e);
    }
  }

  @Override
  @Transactional
  public void delete(final UUID id) {
    repository.deleteById(id);
  }

  @Override
  public Optional<JobApplication> findById(final UUID id) {
    return repository.findById(id).map(JobApplicationMapper::toDomain);
  }

  @Override
  public List<JobApplication> findByUserId(final UserId userId,
                                           @Nullable final ApplicationStatus status,
                                           @Nullable final Source source) {
    final List<JobApplicationEntity> entities;
    if (status != null && source != null) {
      entities = repository.findByUserIdAndStatusAndSourceOrderByDateAppliedDesc(
        userId.value(), status.name(), source.name());
    } else if (status != null) {
      entities = repository.findByUserIdAndStatusOrderByDateAppliedDesc(
        userId.value(), status.name());
    } else if (source != null) {
      entities = repository.findByUserIdAndSourceOrderByDateAppliedDesc(
        userId.value(), source.name());
    } else {
      entities = repository.findByUserIdOrderByDateAppliedDesc(userId.value());
    }
    return entities.stream().map(JobApplicationMapper::toDomain).toList();
  }

  @Override
  public List<JobApplication> findAllByUserId(final UserId userId) {
    return repository.findByUserIdOrderByDateAppliedDesc(userId.value()).stream()
      .map(JobApplicationMapper::toDomain).toList();
  }
}
