package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.domain.model.JobAnalysisRecord;
import dev.jpje.jobtracker.domain.vo.JobAnalysis;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.adapter.JobAnalysisPersistenceAdapter;
import dev.jpje.jobtracker.persistence.entity.JobAnalysisEntity;
import dev.jpje.jobtracker.persistence.entity.JobPostingEntity;
import dev.jpje.jobtracker.persistence.repository.JobAnalysisJpaRepository;
import dev.jpje.jobtracker.persistence.repository.JobPostingJpaRepository;
import dev.jpje.jobtracker.persistence.repository.UserJpaRepository;
import dev.jpje.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class JobAnalysisUpsertIntegrationTest extends GraphQlIntegrationTestBase {

  private static final Instant CREATED_AT = Instant.parse("2026-01-01T00:00:00Z");

  @Autowired
  private UserJpaRepository userRepo;
  @Autowired
  private JobPostingJpaRepository postingRepo;
  @Autowired
  private JobAnalysisJpaRepository analysisRepo;
  @Autowired
  private JobAnalysisPersistenceAdapter adapter;

  @Test
  void shouldReplaceAnalysisInPlaceOnReAnalysis() {
    // Given
    final var userId = userRepo.findByUsername("alexandra").orElseThrow().getId();
    final var postingId = createPosting(userId, "analysis-replace-in-place");
    final var previous = analysisEntity(postingId, userId);
    analysisRepo.saveAndFlush(previous);

    // When
    adapter.saveOrReplace(analysisRecord(postingId, userId, CREATED_AT.plusSeconds(60)));

    // Then
    final var stored = analysisRepo.findByJobPostingId(postingId).orElseThrow();
    assertThat(stored)
      .extracting(JobAnalysisEntity::getId, JobAnalysisEntity::getCreatedAt, JobAnalysisEntity::getSummary)
      .as("re-analysis upserts the row keeping its identity")
      .containsExactly(previous.getId(), CREATED_AT, "replacement summary");
    assertThat(analysisRepo.findAll().stream()
      .filter(entity -> entity.getJobPostingId().equals(postingId)))
      .as("only one analysis per posting").hasSize(1);
  }

  @Test
  void shouldKeepPreviousAnalysisWhenReAnalysisFails() {
    // Given
    final var userId = userRepo.findByUsername("alexandra").orElseThrow().getId();
    final var postingId = createPosting(userId, "analysis-failed-reanalysis");
    analysisRepo.saveAndFlush(analysisEntity(postingId, userId));

    // When
    final var invalidRecord = analysisRecord(postingId, UUID.randomUUID(), CREATED_AT);

    // Then
    assertThatThrownBy(() -> adapter.saveOrReplace(invalidRecord))
      .as("re-analysis with an invalid owner fails")
      .isInstanceOf(DataIntegrityViolationException.class);
    final var stored = analysisRepo.findByJobPostingId(postingId).orElseThrow();
    assertThat(stored.getSummary()).as("failed re-analysis keeps the previous result")
      .isEqualTo("previous summary");
  }

  private UUID createPosting(final UUID userId, final String slug) {
    final var posting = new JobPostingEntity();
    posting.setId(UUID.randomUUID());
    posting.setUserId(userId);
    posting.setUrl("https://example.com/" + slug + "-" + UUID.randomUUID());
    posting.setSource("LINKEDIN");
    posting.setTitle("Engineer");
    posting.setCompany("Acme");
    posting.setDescription("desc");
    posting.setPostedAt(Instant.EPOCH);
    return postingRepo.saveAndFlush(posting).getId();
  }

  private JobAnalysisEntity analysisEntity(final UUID postingId, final UUID userId) {
    final var entity = new JobAnalysisEntity();
    entity.setId(UUID.randomUUID());
    entity.setJobPostingId(postingId);
    entity.setUserId(userId);
    entity.setSummary("previous summary");
    entity.setSeniority("senior");
    entity.setSoftSkills("Teamwork");
    entity.setTechnicalSkills("Java");
    entity.setFitScore(85.0);
    entity.setCompanyRating(4.2);
    entity.setCompanyType("enterprise");
    entity.setSalaryMin(90000.0);
    entity.setSalaryMax(130000.0);
    entity.setSalaryCurrency("USD");
    entity.setCreatedAt(JobAnalysisUpsertIntegrationTest.CREATED_AT);
    return entity;
  }

  private JobAnalysisRecord analysisRecord(final UUID postingId, final UUID userId, final Instant createdAt) {
    return new JobAnalysisRecord(UUID.randomUUID(), postingId, new UserId(userId),
      new JobAnalysis("replacement summary", "senior", List.of("Teamwork"), List.of("Java"),
        85.0, 4.2, "enterprise", 90000.0, 130000.0, "USD"),
      createdAt);
  }
}
