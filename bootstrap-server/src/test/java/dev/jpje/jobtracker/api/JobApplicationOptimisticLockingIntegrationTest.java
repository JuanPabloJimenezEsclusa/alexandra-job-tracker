package dev.jpje.jobtracker.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import dev.jpje.jobtracker.api.config.IntegrationTestConfig;
import dev.jpje.jobtracker.domain.exception.OptimisticLockException;
import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.adapter.JobApplicationPersistenceAdapter;
import dev.jpje.jobtracker.persistence.entity.JobPostingEntity;
import dev.jpje.jobtracker.persistence.repository.JobApplicationJpaRepository;
import dev.jpje.jobtracker.persistence.repository.JobPostingJpaRepository;
import dev.jpje.jobtracker.persistence.repository.UserJpaRepository;
import dev.jpje.jobtracker.server.JobTrackerServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(
  classes = JobTrackerServerApplication.class,
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(IntegrationTestConfig.class)
class JobApplicationOptimisticLockingIntegrationTest extends GraphQlIntegrationTestBase {

  private static final Instant NOW = Instant.EPOCH;

  @Autowired
  private UserJpaRepository userRepo;
  @Autowired
  private JobPostingJpaRepository postingRepo;
  @Autowired
  private JobApplicationJpaRepository appRepo;
  @Autowired
  private JobApplicationPersistenceAdapter adapter;

  @Test
  void shouldIncrementVersionAcrossUpdates() {
    // Given
    final var created = createSavedApplication("version-increments");
    assertThat(created.version()).as("new application version").isEqualTo(0L);

    // When
    final var firstUpdate = adapter.save(created.withStatus(ApplicationStatus.APPLIED, NOW));
    final var secondUpdate = adapter.save(firstUpdate.withStatus(ApplicationStatus.INTERVIEWING, NOW));

    // Then
    assertThat(firstUpdate).extracting(JobApplication::status, JobApplication::version)
      .as("first update").containsExactly(ApplicationStatus.APPLIED, 1L);
    assertThat(secondUpdate).extracting(JobApplication::status, JobApplication::version)
      .as("second update").containsExactly(ApplicationStatus.INTERVIEWING, 2L);
    assertThat(appRepo.findById(created.id()).orElseThrow().getVersion())
      .as("persisted version after two updates").isEqualTo(2L);
  }

  @Test
  void shouldRejectStaleUpdateWithConflict() {
    // Given: another writer commits first, bumping the persisted version
    final var created = createSavedApplication("stale-write-rejected");
    final var snapshot = created.withStatus(ApplicationStatus.APPLIED, NOW);
    final var writer = appRepo.findById(created.id()).orElseThrow();
    writer.setStatus("APPLIED");
    final var writerSaved = appRepo.saveAndFlush(writer);
    assertThat(writerSaved.getVersion()).as("persisted version after concurrent writer").isEqualTo(1L);

    // When, then: the stale update still carries version 0 and must fail
    assertThatThrownBy(() -> adapter.save(snapshot))
      .as("a stale update is rejected instead of overwriting the concurrent write")
      .isInstanceOf(OptimisticLockException.class)
      .hasMessage("Application was modified concurrently");
  }

  private JobApplication createSavedApplication(final String slug) {
    final var userId = userRepo.findByUsername("alexandra").orElseThrow().getId();
    final var posting = new JobPostingEntity();
    posting.setId(UUID.randomUUID());
    posting.setUserId(userId);
    posting.setUrl("https://example.com/" + slug + "-" + UUID.randomUUID());
    posting.setSource("LINKEDIN");
    posting.setTitle("Engineer");
    posting.setCompany("Acme");
    posting.setDescription("desc");
    posting.setPostedAt(NOW);
    postingRepo.saveAndFlush(posting);
    return adapter.save(new JobApplication(UUID.randomUUID(), new UserId(userId),
      posting.getId(), ApplicationStatus.SAVED, NOW, NOW, null, null));
  }
}
