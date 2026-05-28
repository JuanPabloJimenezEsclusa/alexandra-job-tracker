package dev.jpje.jobtracker.cache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobApplication;
import dev.jpje.jobtracker.domain.port.out.CachePort;
import dev.jpje.jobtracker.domain.port.out.LoadJobApplicationPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobApplicationPort;
import dev.jpje.jobtracker.domain.vo.ApplicationStatus;
import dev.jpje.jobtracker.domain.vo.Source;
import dev.jpje.jobtracker.domain.vo.UserId;
import org.jspecify.annotations.Nullable;

public class CachingJobApplicationAdapter implements LoadJobApplicationPort, SaveJobApplicationPort {
  private static final String KEY_PREFIX = "jobapp:";
  private static final String LIST_KEY = "jobapps:user:";

  private final LoadJobApplicationPort loadDelegate;
  private final SaveJobApplicationPort saveDelegate;
  private final CachePort cache;

  public CachingJobApplicationAdapter(final LoadJobApplicationPort loadDelegate,
                                      final SaveJobApplicationPort saveDelegate,
                                      final CachePort cache) {
    this.loadDelegate = loadDelegate;
    this.saveDelegate = saveDelegate;
    this.cache = cache;
  }

  @Override
  public Optional<JobApplication> findById(final UUID id) {
    final var cached = cache.get(KEY_PREFIX + id, JobApplication.class);
    if (cached.isPresent()) {
      return cached;
    }

    final var result = loadDelegate.findById(id);
    result.ifPresent(app -> cache.put(KEY_PREFIX + id, app));
    return result;
  }

  @Override
  public List<JobApplication> findByUserId(final UserId userId,
                                           @Nullable final ApplicationStatus status,
                                           @Nullable final Source source) {
    final var apps = getCachedOrLoad(userId);
    return apps.stream()
      .filter(a -> status == null || a.status() == status)
      .filter(a -> source == null || a.source() == source)
      .toList();
  }

  @Override
  public List<JobApplication> findAllByUserId(final UserId userId) {
    return getCachedOrLoad(userId);
  }

  private List<JobApplication> getCachedOrLoad(final UserId userId) {
    final var key = LIST_KEY + userId.value();
    final var cached = cache.get(key, JobApplicationList.class);
    if (cached.isPresent()) {
      return cached.get().apps();
    }

    final var apps = loadDelegate.findAllByUserId(userId);
    cache.put(key, new JobApplicationList(apps));
    return apps;
  }

  @Override
  public JobApplication save(final JobApplication application) {
    final var saved = saveDelegate.save(application);
    evictUserCaches(application.userId());
    cache.evict(KEY_PREFIX + saved.id());
    cache.put(KEY_PREFIX + saved.id(), saved);
    return saved;
  }

  @Override
  public void delete(final UUID id) {
    final var app = findById(id);
    saveDelegate.delete(id);
    cache.evict(KEY_PREFIX + id);
    app.flatMap(a -> Optional.of(a.userId())).ifPresent(this::evictUserCaches);
  }

  private void evictUserCaches(final UserId userId) {
    cache.evict(LIST_KEY + userId.value());
  }

  private record JobApplicationList(List<JobApplication> apps) {
  }
}
