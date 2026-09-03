package dev.jpje.jobtracker.cache;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.domain.model.JobPosting;
import dev.jpje.jobtracker.domain.port.out.CachePort;
import dev.jpje.jobtracker.domain.port.out.LoadJobPostingPort;
import dev.jpje.jobtracker.domain.port.out.SaveJobPostingPort;
import dev.jpje.jobtracker.domain.vo.UserId;

public class CachingJobPostingAdapter implements LoadJobPostingPort, SaveJobPostingPort {
  private static final String KEY_PREFIX = "jobpost:";
  private static final String LIST_KEY = "jobposts:user:";

  private final LoadJobPostingPort loadDelegate;
  private final SaveJobPostingPort saveDelegate;
  private final CachePort cache;

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
  public Optional<JobPosting> findByIdAndUser(final UUID id, final UserId userId) {
    return findById(id).filter(posting -> posting.userId().equals(userId));
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
