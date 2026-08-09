package dev.jpje.jobtracker.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.port.out.LoadJobAnalysisPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobAnalysisPort;
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
  public void saveOrReplace(final JobAnalysisRecord record) {
    repository.deleteByJobPostingId(record.jobPostingId());
    repository.save(JobAnalysisMapper.toEntity(record));
  }

  @Override
  @Transactional
  public void delete(final UUID id) {
    repository.deleteById(id);
  }

  @Override
  public Optional<JobAnalysisRecord> findById(final UUID id) {
    return repository.findById(id).map(JobAnalysisMapper::toDomain);
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
