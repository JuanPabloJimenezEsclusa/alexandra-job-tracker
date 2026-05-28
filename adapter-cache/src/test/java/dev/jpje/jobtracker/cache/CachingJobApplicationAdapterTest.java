package dev.jpje.jobtracker.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
import dev.jpje.jobtracker.domain.vo.CompanyName;
import dev.jpje.jobtracker.domain.vo.Notes;
import dev.jpje.jobtracker.domain.vo.RoleName;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.Url;
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

    assertThat(adapter.findById(app.id())).hasValue(app);
    verify(loadDelegate, times(1)).findById(app.id());
  }

  @Test
  void shouldLoadAndCacheApplicationOnMiss() {
    final var app = jobApplication();
    when(loadDelegate.findById(app.id())).thenReturn(Optional.of(app));

    assertThat(adapter.findById(app.id())).hasValue(app);
  }

  @Test
  void shouldReturnCachedListWhenPresent() {
    final var userId = UserId.generate();
    final var app = jobApplication(userId);
    primeListCache(userId, List.of(app));

    assertThat(adapter.findByUserId(userId, null, null)).containsExactly(app);
    verify(loadDelegate, times(1)).findAllByUserId(userId);
  }

  @Test
  void shouldLoadAndCacheListOnMiss() {
    final var userId = UserId.generate();
    final var app = jobApplication(userId);
    when(loadDelegate.findAllByUserId(userId)).thenReturn(List.of(app));

    assertThat(adapter.findAllByUserId(userId)).isEqualTo(List.of(app));
  }

  @Test
  void shouldFilterCachedApplicationsByStatus() {
    final var userId = UserId.generate();
    final var saved = jobApplication(userId, ApplicationStatus.SAVED);
    final var applied = jobApplication(userId, ApplicationStatus.APPLIED);
    primeListCache(userId, List.of(saved, applied));

    assertThat(adapter.findByUserId(userId, ApplicationStatus.SAVED, null))
      .extracting(JobApplication::status)
      .containsExactly(ApplicationStatus.SAVED);
  }

  @Test
  void shouldFilterCachedApplicationsBySource() {
    final var userId = UserId.generate();
    final var linkedIn = jobApplication(userId, Source.LINKEDIN);
    final var indeed = jobApplication(userId, Source.INDEED);
    primeListCache(userId, List.of(linkedIn, indeed));

    assertThat(adapter.findByUserId(userId, null, Source.INDEED))
      .extracting(JobApplication::source)
      .containsExactly(Source.INDEED);
  }

  @Test
  void shouldSaveAndCacheApplication() {
    final var app = jobApplication();

    adapter.save(app);

    assertThat(cache.asMap()).containsKey("jobapp:" + app.id());
    verify(saveDelegate).save(app);
  }

  @Test
  void shouldDeleteAndEvictCaches() {
    final var app = jobApplication();
    when(loadDelegate.findById(app.id())).thenReturn(Optional.of(app));

    adapter.delete(app.id());

    assertThat(cache.asMap()).doesNotContainKey("jobapp:" + app.id());
    verify(saveDelegate).delete(app.id());
  }

  private void primeByIdCache(final UUID id, final JobApplication app) {
    when(loadDelegate.findById(id)).thenReturn(Optional.of(app));
    adapter.findById(id);
  }

  private void primeListCache(final UserId userId, final List<JobApplication> apps) {
    when(loadDelegate.findAllByUserId(userId)).thenReturn(apps);
    adapter.findByUserId(userId, null, null);
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
      .set(field(JobApplication::company), CompanyName.of("Acme"))
      .set(field(JobApplication::role), RoleName.of("SWE"))
      .set(field(JobApplication::source), Source.LINKEDIN)
      .set(field(JobApplication::postingUrl), Url.of("https://example.com/job"))
      .set(field(JobApplication::notes), Notes.of("notes"))
      .set(field(JobApplication::dateApplied), Instant.EPOCH)
      .set(field(JobApplication::lastUpdated), Instant.EPOCH)
      .create();
  }

  private static JobApplication jobApplication(final UserId userId, final Source source) {
    return Instancio.of(JobApplication.class)
      .set(field(JobApplication::userId), userId)
      .set(field(JobApplication::status), ApplicationStatus.SAVED)
      .set(field(JobApplication::company), CompanyName.of("Acme"))
      .set(field(JobApplication::role), RoleName.of("SWE"))
      .set(field(JobApplication::source), source)
      .set(field(JobApplication::postingUrl), Url.of("https://example.com/job"))
      .set(field(JobApplication::notes), Notes.of("notes"))
      .set(field(JobApplication::dateApplied), Instant.EPOCH)
      .set(field(JobApplication::lastUpdated), Instant.EPOCH)
      .create();
  }
}
