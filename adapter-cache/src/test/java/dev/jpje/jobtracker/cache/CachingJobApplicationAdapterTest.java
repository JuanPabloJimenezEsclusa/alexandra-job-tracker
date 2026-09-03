package dev.jpje.jobtracker.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.out.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CachingJobApplicationAdapterTest {

  @Mock
  private LoadJobApplicationPort loadDelegate;

  @Mock
  private SaveJobApplicationPort saveDelegate;

  @Spy
  private CaffeineCacheAdapter cache = new CaffeineCacheAdapter(100, Duration.ofMinutes(5));

  @InjectMocks
  private CachingJobApplicationAdapter adapter;

  @Test
  void shouldReturnCachedApplicationWhenPresent() {
    final var app = jobApplication();
    primeByIdCache(app.id(), app);

    assertThat(adapter.findById(app.id())).as("cached application should be returned").hasValue(app);
    verify(loadDelegate, description("load delegate should be hit only on cache miss")).findById(app.id());
  }

  @Test
  void shouldLoadAndCacheApplicationOnMiss() {
    // Given
    final var app = jobApplication();
    when(loadDelegate.findById(app.id())).thenReturn(Optional.of(app));

    // When
    assertThat(adapter.findById(app.id())).as("loaded application returned").hasValue(app);

    // Then
    assertThat(adapter.findById(app.id())).as("second read served from cache").hasValue(app);
    verify(loadDelegate).findById(app.id());
  }

  @Test
  void shouldReturnCachedListWhenPresent() {
    final var userId = UserId.generate();
    final var app = jobApplication(userId);
    primeListCache(userId, List.of(app));

    assertThat(adapter.findByUserId(userId, null)).as("cached list should be returned").containsExactly(app);
    verify(loadDelegate, description("load delegate should be hit only on cache miss")).findAllByUserId(userId);
  }

  @Test
  void shouldLoadAndCacheListOnMiss() {
    // Given
    final var userId = UserId.generate();
    final var app = jobApplication(userId);
    when(loadDelegate.findAllByUserId(userId)).thenReturn(List.of(app));

    // When
    assertThat(adapter.findAllByUserId(userId)).as("loaded list returned").isEqualTo(List.of(app));

    // Then
    assertThat(adapter.findAllByUserId(userId)).as("second read served from cache").isEqualTo(List.of(app));
    verify(loadDelegate).findAllByUserId(userId);
  }

  @Test
  void shouldFilterCachedApplicationsByStatus() {
    final var userId = UserId.generate();
    final var saved = jobApplication(userId, ApplicationStatus.SAVED);
    final var applied = jobApplication(userId, ApplicationStatus.APPLIED);
    primeListCache(userId, List.of(saved, applied));

    assertThat(adapter.findByUserId(userId, ApplicationStatus.SAVED))
      .extracting(JobApplication::status)
      .containsExactly(ApplicationStatus.SAVED);
  }

  @Test
  void shouldScopeApplicationByIdToOwner() {
    // Given
    final var owner = UserId.generate();
    final var app = jobApplication(owner);
    primeByIdCache(app.id(), app);

    // When, then
    assertThat(adapter.findByIdAndUser(app.id(), owner)).as("owned application returned").hasValue(app);
    assertThat(adapter.findByIdAndUser(app.id(), UserId.generate()))
      .as("another user's application indistinguishable from missing").isEmpty();
    verify(loadDelegate, description("load delegate should be hit only on cache miss")).findById(app.id());
  }

  @Test
  void shouldReturnEmptyWhenApplicationMissingForUser() {
    // Given
    final var userId = UserId.generate();
    final var id = UUID.randomUUID();
    when(loadDelegate.findById(id)).thenReturn(Optional.empty());

    // When, then
    assertThat(adapter.findByIdAndUser(id, userId)).as("missing application scoped by user").isEmpty();
    verify(loadDelegate, description("load delegate should be hit on a scoped miss")).findById(id);
  }

  @Test
  void shouldSaveAndCacheApplication() {
    // Given
    final var app = jobApplication();
    when(saveDelegate.save(app)).thenReturn(app);

    // When
    adapter.save(app);

    // Then
    assertThat(adapter.findById(app.id())).as("saved application served from cache").hasValue(app);
    assertThat(cache.asMap()).as("saved application should be cached").containsKey("jobapp:" + app.id());
    verify(saveDelegate, description("save should be delegated")).save(app);
    verifyNoInteractions(loadDelegate);
  }

  @Test
  void shouldDeleteAndEvictCaches() {
    // Given
    final var app = jobApplication();
    when(loadDelegate.findById(app.id())).thenReturn(Optional.of(app));

    // When
    adapter.delete(app.id());

    // Then
    when(loadDelegate.findById(app.id())).thenReturn(Optional.empty());
    assertThat(adapter.findById(app.id())).as("evicted application reloaded from delegate").isEmpty();
    assertThat(cache.asMap()).as("deleted application should be evicted from cache").doesNotContainKey("jobapp:" + app.id());
    verify(saveDelegate, description("delete should be delegated")).delete(app.id());
    verify(loadDelegate, times(2)).findById(app.id());
  }

  private void primeByIdCache(final UUID id, final JobApplication app) {
    when(loadDelegate.findById(id)).thenReturn(Optional.of(app));
    adapter.findById(id);
  }

  private void primeListCache(final UserId userId, final List<JobApplication> apps) {
    when(loadDelegate.findAllByUserId(userId)).thenReturn(apps);
    adapter.findByUserId(userId, null);
  }

  private static JobApplication jobApplication() {
    return jobApplication(UserId.generate(), ApplicationStatus.SAVED);
  }

  private static JobApplication jobApplication(final UserId userId) {
    return jobApplication(userId, ApplicationStatus.SAVED);
  }

  private static JobApplication jobApplication(final UserId userId, final ApplicationStatus status) {
    return Instancio.of(JobApplication.class)
      .set(field(JobApplication::userId), userId)
      .set(field(JobApplication::status), status)
      .set(field(JobApplication::notes), Notes.of("notes"))
      .set(field(JobApplication::dateApplied), Instant.EPOCH)
      .set(field(JobApplication::lastUpdated), Instant.EPOCH)
      .create();
  }
}
