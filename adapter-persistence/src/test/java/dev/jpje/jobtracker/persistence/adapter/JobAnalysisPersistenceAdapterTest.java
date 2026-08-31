package dev.jpje.jobtracker.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
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
    // Given
    final var jobAnalysisRecord = jobAnalysisRecord();

    // When
    adapter.saveOrReplace(jobAnalysisRecord);

    // Then
    verify(repository, description("existing analysis deleted by posting")).deleteByJobPostingId(jobAnalysisRecord.jobPostingId());
    verify(repository, description("replacement analysis persisted")).save(any(JobAnalysisEntity.class));
  }

  @Test
  void shouldDeleteById() {
    // Given
    final var id = UUID.randomUUID();

    // When
    adapter.delete(id);

    // Then
    verify(repository, description("repository delete invoked")).deleteById(id);
  }

  @Test
  void shouldFindById() {
    // Given
    final var entity = jobAnalysisEntity();
    when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

    // When, then
    assertThat(adapter.findById(entity.getId())).as("found analysis").contains(expected(entity));
  }

  @Test
  void shouldFindByJobPostingId() {
    // Given
    final var entity = jobAnalysisEntity();
    when(repository.findByJobPostingId(entity.getJobPostingId())).thenReturn(Optional.of(entity));

    // When, then
    assertThat(adapter.findByJobPostingId(entity.getJobPostingId())).as("analysis by posting").contains(expected(entity));
  }

  @Test
  void shouldFindByUserId() {
    // Given
    final var userId = UserId.generate();
    final var entity = jobAnalysisEntity(userId);
    when(repository.findByUserIdOrderByCreatedAtDesc(userId.value())).thenReturn(List.of(entity));

    // When, then
    assertThat(adapter.findByUserId(userId)).as("analyses for user").containsExactly(expected(entity));
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

  private static JobAnalysisRecord jobAnalysisRecord() {
    return new JobAnalysisRecord(UUID.randomUUID(), UUID.randomUUID(), UserId.generate(),
      jobAnalysis(), Instant.EPOCH);
  }

  private static JobAnalysisEntity jobAnalysisEntity() {
    return jobAnalysisEntity(UserId.generate());
  }

  private static JobAnalysisEntity jobAnalysisEntity(final UserId userId) {
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

  private static JobAnalysis jobAnalysis() {
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
