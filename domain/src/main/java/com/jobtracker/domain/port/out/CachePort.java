package com.jobtracker.domain.port.out;

import java.util.Optional;

/**
 * Port for caching data with generic key-value operations.
 */
public interface CachePort {
  /**
   * Retrieves a cached value by key, or empty if not found.
   */
  <T> Optional<T> get(String key, Class<T> type);

  /**
   * Stores a value in the cache with the given key.
   */
  void put(String key, Object value);

  /**
   * Removes a single entry from the cache by key.
   */
  void evict(String key);

  /**
   * Clears all entries from the cache.
   */
  void clear();
}
