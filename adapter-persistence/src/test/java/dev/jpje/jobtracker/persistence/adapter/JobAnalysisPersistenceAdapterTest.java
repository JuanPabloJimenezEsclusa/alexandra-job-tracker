package dev.jpje.jobtracker.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobAnalysisEntity;
import dev.jpje.jobtracker.persistence.repository.JobAnalysisJpaRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobAnalysisPersistenceAdapterTest {

  @Mock
  private JobAnalysisJpaRepository repository;

  @InjectMocks
  private JobAnalysisPersistenceAdapter adapter;

  @Test
  void shouldSaveOrReplaceRecord() {
    final var record = record();

    assertThatCode(() -> adapter.saveOrReplace(record)).doesNotThrowAnyException();
    verify(repository).deleteByJobPostingId(record.jobPostingId());
    verify(repository).save(any(JobAnalysisEntity.class));
  }

  @Test
  void shouldDeleteById() {
    final var id = UUID.randomUUID();

    assertThatCode(() -> adapter.delete(id)).doesNotThrowAnyException();
    verify(repository).deleteById(id);
  }

  @Test
  void shouldFindById() {
    final var entity = entity();
    when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

    assertThat(adapter.findById(entity.getId())).contains(expected(entity));
  }

  @Test
  void shouldFindByJobPostingId() {
    final var entity = entity();
    when(repository.findByJobPostingId(entity.getJobPostingId())).thenReturn(Optional.of(entity));

    assertThat(adapter.findByJobPostingId(entity.getJobPostingId())).contains(expected(entity));
  }

  @Test
  void shouldFindByUserId() {
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserIdOrderByCreatedAtDesc(userId.value())).thenReturn(List.of(entity));

    assertThat(adapter.findByUserId(userId)).containsExactly(expected(entity));
  }

  private static JobAnalysisRecord expected(final JobAnalysisEntity entity) {
    return new JobAnalysisRecord(entity.getId(), entity.getJobPostingId(),
      new UserId(entity.getUserId()),
      new JobAnalysis(entity.getSummary(), entity.getSeniority(),
        List.of("Teamwork"), List.of("Java", "Spring"),
        entity.getFitScore(), entity.getCompanyRating(), entity.getCompanyType(),
        entity.getSalaryMin(), entity.getSalaryMax(), entity.getSalaryCurrency()),
      entity.getCreatedAt());
  }

  private static JobAnalysisRecord record() {
    return new JobAnalysisRecord(UUID.randomUUID(), UUID.randomUUID(), UserId.generate(),
      analysis(), Instant.EPOCH);
  }

  private static JobAnalysisEntity entity() {
    return entity(UserId.generate());
  }

  private static JobAnalysisEntity entity(final UserId userId) {
    final var entity = new JobAnalysisEntity();
    entity.setId(UUID.randomUUID());
    entity.setJobPostingId(UUID.randomUUID());
    entity.setUserId(userId.value());
    entity.setSummary("Java backend role");
    entity.setSeniority("senior");
    entity.setSoftSkills("Teamwork");
    entity.setTechnicalSkills("Java,Spring");
    entity.setFitScore(85.0);
    entity.setCompanyRating(4.2);
    entity.setCompanyType("enterprise");
    entity.setSalaryMin(90000.0);
    entity.setSalaryMax(130000.0);
    entity.setSalaryCurrency("USD");
    entity.setCreatedAt(Instant.EPOCH);
    return entity;
  }

  private static JobAnalysis analysis() {
    return Instancio.of(JobAnalysis.class)
      .set(field(JobAnalysis::fitScore), 85.0)
      .set(field(JobAnalysis::companyRating), 4.2)
      .set(field(JobAnalysis::companyType), "enterprise")
      .set(field(JobAnalysis::salaryMin), 90000.0)
      .set(field(JobAnalysis::salaryMax), 130000.0)
      .set(field(JobAnalysis::salaryCurrency), "USD")
      .create();
  }
}
