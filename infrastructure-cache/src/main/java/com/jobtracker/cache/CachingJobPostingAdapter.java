package com.jobtracker.cache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.jobtracker.domain.model.JobPosting;
import com.jobtracker.domain.port.out.CachePort;
import com.jobtracker.domain.port.out.LoadJobPostingPort;
import com.jobtracker.domain.port.out.SaveJobPostingPort;
import com.jobtracker.domain.vo.UserId;

/**
 * Caching decorator over {@link LoadJobPostingPort} and {@link SaveJobPostingPort}.
 */
public class CachingJobPostingAdapter implements LoadJobPostingPort, SaveJobPostingPort {
  private static final String KEY_PREFIX = "jobpost:";
  private static final String LIST_KEY = "jobposts:user:";

  private final LoadJobPostingPort loadDelegate;
  private final SaveJobPostingPort saveDelegate;
  private final CachePort cache;

  /**
   * Wraps delegate ports with a cache layer.
   */
  public CachingJobPostingAdapter(final LoadJobPostingPort loadDelegate,
                                  final SaveJobPostingPort saveDelegate,
                                  final CachePort cache) {
    this.loadDelegate = loadDelegate;
    this.saveDelegate = saveDelegate;
    this.cache = cache;
  }

  @Override
  public Optional<JobPosting> findById(final UUID id) {
    final var cached = cache.get(KEY_PREFIX + id, JobPosting.class);
    if (cached.isPresent()) {
      return cached;
    }

    final var result = loadDelegate.findById(id);
    result.ifPresent(p -> cache.put(KEY_PREFIX + id, p));
    return result;
  }

  @Override
  public List<JobPosting> findByUserId(final UserId userId) {
    final var key = LIST_KEY + userId.value();
    final var cached = cache.get(key, JobPostingList.class);
    if (cached.isPresent()) {
      return cached.get().list();
    }

    final var result = loadDelegate.findByUserId(userId);
    cache.put(key, new JobPostingList(result));
    return result;
  }

  @Override
  public void save(final JobPosting posting) {
    saveDelegate.save(posting);
    cache.evict(LIST_KEY + posting.userId().value());
    cache.put(KEY_PREFIX + posting.id(), posting);
  }

  private record JobPostingList(List<JobPosting> list) {
  }
}
