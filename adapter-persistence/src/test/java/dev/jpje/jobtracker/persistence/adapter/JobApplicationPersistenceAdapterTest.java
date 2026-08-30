package dev.jpje.jobtracker.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import dev.jpje.jobtracker.persistence.entity.JobApplicationEntity;
import dev.jpje.jobtracker.persistence.repository.JobApplicationJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JobApplicationPersistenceAdapterTest {

  private static final int SINGLE_RESULT_SIZE = 1;

  @Mock
  private JobApplicationJpaRepository repository;

  @InjectMocks
  private JobApplicationPersistenceAdapter adapter;

  @Test
  void shouldSaveApplication() {
    final var entity = entity();
    when(repository.saveAndFlush(any(JobApplicationEntity.class))).thenReturn(entity);

    final var saved = adapter.save(application());

    assertThat(saved).as("saved application returned").isEqualTo(toDomain(entity));
    verify(repository, description("repository invoked with flush")).saveAndFlush(any(JobApplicationEntity.class));
  }

  @Test
  void shouldDeleteApplication() {
    final var id = UUID.randomUUID();

    assertThatCode(() -> adapter.delete(id)).as("delete does not throw").doesNotThrowAnyException();
    verify(repository, description("repository delete invoked")).deleteById(id);
  }

  @Test
  void shouldFindById() {
    final var entity = entity();
    when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));

    assertThat(adapter.findById(entity.getId())).hasValue(toDomain(entity));
  }

  @Test
  void shouldReturnEmptyWhenNotFound() {
    final var id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    assertThat(adapter.findById(id)).isEmpty();
  }

  @Test
  void shouldFindByUserAndStatus() {
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserIdAndStatusOrderByDateAppliedDesc(
      userId.value(), "SAVED")).thenReturn(List.of(entity));

    assertThat(adapter.findByUserId(userId, ApplicationStatus.SAVED))
      .as("single result list size").hasSize(SINGLE_RESULT_SIZE);
  }

  @Test
  void shouldFindAllByUser() {
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserIdOrderByDateAppliedDesc(userId.value())).thenReturn(List.of(entity));

    assertThat(adapter.findByUserId(userId, null)).as("single result list size").hasSize(SINGLE_RESULT_SIZE);
  }

  @Test
  void shouldFindAllByUserId() {
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserIdOrderByDateAppliedDesc(userId.value())).thenReturn(List.of(entity));

    assertThat(adapter.findAllByUserId(userId)).as("single result list size").hasSize(SINGLE_RESULT_SIZE);
  }

  private static JobApplication toDomain(final JobApplicationEntity entity) {
    return new JobApplication(
      entity.getId(),
      new UserId(entity.getUserId()),
      entity.getJobPostingId(),
      ApplicationStatus.valueOf(entity.getStatus()),
      entity.getDateApplied(),
      entity.getLastUpdated(),
      entity.getNotes() != null ? Notes.of(entity.getNotes()) : null,
      entity.getVersion());
  }

  private static JobApplication application() {
    final var userId = UserId.generate();
    return new JobApplication(UUID.randomUUID(), userId, UUID.randomUUID(),
      ApplicationStatus.SAVED, Instant.EPOCH, Instant.EPOCH, Notes.of("notes"), 0L);
  }

  private static JobApplicationEntity entity() {
    return entity(UserId.generate());
  }

  private static JobApplicationEntity entity(final UserId userId) {
    final var entity = new JobApplicationEntity();
    entity.setId(UUID.randomUUID());
    entity.setUserId(userId.value());
    entity.setJobPostingId(UUID.randomUUID());
    entity.setStatus("SAVED");
    entity.setDateApplied(Instant.EPOCH);
    entity.setLastUpdated(Instant.EPOCH);
    entity.setNotes("notes");
    return entity;
  }
}
