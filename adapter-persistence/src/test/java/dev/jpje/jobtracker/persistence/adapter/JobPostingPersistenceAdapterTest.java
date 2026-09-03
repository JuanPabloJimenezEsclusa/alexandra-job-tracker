package dev.jpje.jobtracker.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.exception.ResourceAlreadyExistsException;
import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.JobTitle;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobPostingEntity;
import dev.jpje.jobtracker.persistence.repository.JobPostingJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class JobPostingPersistenceAdapterTest {

  private static final int SINGLE_RESULT_SIZE = 1;

  @Mock
  private JobPostingJpaRepository repository;

  @InjectMocks
  private JobPostingPersistenceAdapter adapter;

  @Test
  void shouldSavePosting() {
    // Given
    final var posting = posting();

    // When
    adapter.save(posting);

    // Then
    verify(repository, description("posting persisted with flush")).saveAndFlush(any(JobPostingEntity.class));
  }

  @Test
  void shouldMapDuplicateUrlToConflict() {
    // Given
    when(repository.saveAndFlush(any(JobPostingEntity.class)))
      .thenThrow(new DataIntegrityViolationException("duplicate user_id, url"));
    final var posting = posting();

    // When, then
    assertThatThrownBy(() -> adapter.save(posting))
      .as("duplicate url submission maps to a conflict")
      .isInstanceOf(ResourceAlreadyExistsException.class)
      .hasMessage("Job posting already exists");
  }

  @Test
  void shouldFindById() {
    // Given
    final var entity = entity();
    when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

    // When, then
    assertThat(adapter.findById(entity.getId())).as("found posting").hasValue(toDomain(entity));
  }

  @Test
  void shouldReturnEmptyWhenNotFound() {
    // Given
    final var id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    // When, then
    assertThat(adapter.findById(id)).as("missing posting").isEmpty();
  }

  @Test
  void shouldFindByIdAndUser() {
    // Given
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByIdAndUserId(entity.getId(), userId.value())).thenReturn(Optional.of(entity));

    // When, then
    assertThat(adapter.findByIdAndUser(entity.getId(), userId)).as("posting scoped by user").hasValue(toDomain(entity));
  }

  @Test
  void shouldReturnEmptyWhenFindByIdAndUserMisses() {
    // Given
    final var id = UUID.randomUUID();
    final var userId = UserId.generate();
    when(repository.findByIdAndUserId(id, userId.value())).thenReturn(Optional.empty());

    // When, then
    assertThat(adapter.findByIdAndUser(id, userId)).as("missing or foreign posting").isEmpty();
  }

  @Test
  void shouldFindByUserId() {
    // Given
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserId(userId.value())).thenReturn(List.of(entity));

    // When, then
    assertThat(adapter.findByUserId(userId)).as("single result list size").hasSize(SINGLE_RESULT_SIZE);
  }

  private static JobPosting toDomain(final JobPostingEntity entity) {
    return new JobPosting(entity.getId(), new UserId(entity.getUserId()),
      Url.of(entity.getUrl()), Source.valueOf(entity.getSource()),
      JobTitle.of(entity.getTitle()), CompanyName.of(entity.getCompany()),
      entity.getDescription(), entity.getPostedAt());
  }

  private static JobPosting posting() {
    return new JobPosting(UUID.randomUUID(), UserId.generate(), Url.of("https://example.com/job"),
      Source.LINKEDIN, JobTitle.of("Engineer"), CompanyName.of("Acme"), "desc", Instant.EPOCH);
  }

  private static JobPostingEntity entity() {
    return entity(UserId.generate());
  }

  private static JobPostingEntity entity(final UserId userId) {
    final var entity = new JobPostingEntity();
    entity.setId(UUID.randomUUID());
    entity.setUserId(userId.value());
    entity.setUrl("https://example.com/job");
    entity.setSource("LINKEDIN");
    entity.setTitle("Engineer");
    entity.setCompany("Acme");
    entity.setDescription("desc");
    entity.setPostedAt(Instant.EPOCH);
    return entity;
  }
}
