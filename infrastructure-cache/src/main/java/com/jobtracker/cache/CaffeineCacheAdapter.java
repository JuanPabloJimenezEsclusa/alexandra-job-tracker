package com.jobtracker.cache;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.jobtracker.domain.port.out.CachePort;

public class CaffeineCacheAdapter implements CachePort {

  private final Cache<String, Object> cache;

  public CaffeineCacheAdapter(final int maxSize, final Duration defaultTtl) {
    this.cache = Caffeine.newBuilder()
      .maximumSize(maxSize)
      .expireAfterWrite(defaultTtl)
      .recordStats()
      .build();
  }

  @Override
  public <T> Optional<T> get(final String key, final Class<T> type) {
    final var value = cache.getIfPresent(key);
    if (value == null) return Optional.empty();
    try {
      return Optional.of(type.cast(value));
    } catch (ClassCastException e) {
      evict(key);
      return Optional.empty();
    }
  }

  @Override
  public void put(final String key, final Object value) {
    cache.put(key, value);
  }

  @Override
  public void evict(final String key) {
    cache.invalidate(key);
  }

  @Override
  public void clear() {
    cache.invalidateAll();
  }

  public ConcurrentMap<String, Object> asMap() {
    return cache.asMap();
  }
}
