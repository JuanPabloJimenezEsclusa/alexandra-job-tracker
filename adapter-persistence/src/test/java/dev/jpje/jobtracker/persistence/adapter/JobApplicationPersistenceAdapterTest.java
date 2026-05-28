package dev.jpje.jobtracker.persistence.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.RoleName;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
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

  @Mock
  private JobApplicationJpaRepository repository;

  @InjectMocks
  private JobApplicationPersistenceAdapter adapter;

  @Test
  void shouldSaveApplication() {
    final var app = application();

    assertThatCode(() -> adapter.save(app)).doesNotThrowAnyException();
    verify(repository).save(any(JobApplicationEntity.class));
  }

  @Test
  void shouldDeleteApplication() {
    final var id = UUID.randomUUID();

    assertThatCode(() -> adapter.delete(id)).doesNotThrowAnyException();
    verify(repository).deleteById(id);
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
  void shouldFindByUserAndStatusAndSource() {
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserIdAndStatusAndSourceOrderByDateAppliedDesc(
      userId.value(), "SAVED", "LINKEDIN")).thenReturn(List.of(entity));

    assertThat(adapter.findByUserId(userId, ApplicationStatus.SAVED, Source.LINKEDIN))
      .hasSize(1);
  }

  @Test
  void shouldFindByUserAndStatus() {
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserIdAndStatusOrderByDateAppliedDesc(
      userId.value(), "SAVED")).thenReturn(List.of(entity));

    assertThat(adapter.findByUserId(userId, ApplicationStatus.SAVED, null))
      .hasSize(1);
  }

  @Test
  void shouldFindByUserAndSource() {
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserIdAndSourceOrderByDateAppliedDesc(
      userId.value(), "LINKEDIN")).thenReturn(List.of(entity));

    assertThat(adapter.findByUserId(userId, null, Source.LINKEDIN))
      .hasSize(1);
  }

  @Test
  void shouldFindAllByUser() {
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserIdOrderByDateAppliedDesc(userId.value())).thenReturn(List.of(entity));

    assertThat(adapter.findByUserId(userId, null, null)).hasSize(1);
  }

  @Test
  void shouldFindAllByUserId() {
    final var userId = UserId.generate();
    final var entity = entity(userId);
    when(repository.findByUserIdOrderByDateAppliedDesc(userId.value())).thenReturn(List.of(entity));

    assertThat(adapter.findAllByUserId(userId)).hasSize(1);
  }

  private static JobApplication toDomain(final JobApplicationEntity entity) {
    return new JobApplication(entity.getId(), new UserId(entity.getUserId()),
      CompanyName.of(entity.getCompany()), RoleName.of(entity.getRole()),
      Source.valueOf(entity.getSource()), Url.of(entity.getPostingUrl()),
      ApplicationStatus.valueOf(entity.getStatus()), entity.getDateApplied(),
      entity.getLastUpdated(), Notes.of(entity.getNotes()));
  }

  private static JobApplication application() {
    final var userId = UserId.generate();
    return new JobApplication(UUID.randomUUID(), userId, CompanyName.of("Acme"),
      RoleName.of("SWE"), Source.LINKEDIN, Url.of("https://example.com/job"),
      ApplicationStatus.SAVED, Instant.EPOCH, Instant.EPOCH, Notes.of("notes"));
  }

  private static JobApplicationEntity entity() {
    return entity(UserId.generate());
  }

  private static JobApplicationEntity entity(final UserId userId) {
    final var entity = new JobApplicationEntity();
    entity.setId(UUID.randomUUID());
    entity.setUserId(userId.value());
    entity.setCompany("Acme");
    entity.setRole("SWE");
    entity.setSource("LINKEDIN");
    entity.setPostingUrl("https://example.com/job");
    entity.setStatus("SAVED");
    entity.setDateApplied(Instant.EPOCH);
    entity.setLastUpdated(Instant.EPOCH);
    entity.setNotes("notes");
    return entity;
  }
}
