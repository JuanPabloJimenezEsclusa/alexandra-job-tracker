package dev.jpje.jobtracker.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.exception.OptimisticLockException;
import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.outbound.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobApplicationEntity;
import dev.jpje.jobtracker.persistence.mapper.JobApplicationMapper;
import dev.jpje.jobtracker.persistence.repository.JobApplicationJpaRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
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
    } catch (final OptimisticLockingFailureException e) {
      throw new OptimisticLockException("Application was modified concurrently", e);
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
  public Optional<JobApplication> findByIdAndUser(final UUID id, final UserId userId) {
    return repository.findByIdAndUserId(id, userId.value()).map(JobApplicationMapper::toDomain);
  }

  @Override
  public List<JobApplication> findByUserId(final UserId userId,
                                           @Nullable final ApplicationStatus status) {
    final List<JobApplicationEntity> entities = status != null
      ? repository.findByUserIdAndStatusOrderByDateAppliedDesc(userId.value(), status.name())
      : repository.findByUserIdOrderByDateAppliedDesc(userId.value());
    return entities.stream().map(JobApplicationMapper::toDomain).toList();
  }

  @Override
  public List<JobApplication> findAllByUserId(final UserId userId) {
    return repository.findByUserIdOrderByDateAppliedDesc(userId.value()).stream()
      .map(JobApplicationMapper::toDomain).toList();
  }
}
