package dev.jpje.jobtracker.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.port.outbound.LoadJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.outbound.SaveJobAnalysisPort;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.mapper.JobAnalysisMapper;
import dev.jpje.jobtracker.persistence.repository.JobAnalysisJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
public class JobAnalysisPersistenceAdapter implements SaveJobAnalysisPort, LoadJobAnalysisPort {
  private final JobAnalysisJpaRepository repository;

  public JobAnalysisPersistenceAdapter(final JobAnalysisJpaRepository repository) {
    this.repository = repository;
  }

  @Override
  @Transactional
  public void saveOrReplace(final JobAnalysisRecord jobAnalysisRecord) {
    final var existing = repository.findByJobPostingId(jobAnalysisRecord.jobPostingId());
    final var entity = JobAnalysisMapper.toEntity(jobAnalysisRecord);
    existing.ifPresent(previous -> {
      entity.setId(previous.getId());
      entity.setCreatedAt(previous.getCreatedAt());
    });
    repository.saveAndFlush(entity);
  }

  @Override
  @Transactional
  public void delete(final UUID id) {
    repository.deleteById(id);
  }

  @Override
  public Optional<JobAnalysisRecord> findByIdAndUser(final UUID id, final UserId userId) {
    return repository.findByIdAndUserId(id, userId.value()).map(JobAnalysisMapper::toDomain);
  }

  @Override
  public Optional<JobAnalysisRecord> findByJobPostingId(final UUID jobPostingId) {
    return repository.findByJobPostingId(jobPostingId).map(JobAnalysisMapper::toDomain);
  }

  @Override
  public List<JobAnalysisRecord> findByUserId(final UserId userId) {
    return repository.findByUserIdOrderByCreatedAtDesc(userId.value())
      .stream()
      .map(JobAnalysisMapper::toDomain)
      .toList();
  }
}
