package com.jobtracker.domain.port.out;

import java.time.Duration;
import java.util.Optional;

public interface CachePort {
  <T> Optional<T> get(String key, Class<T> type);
  void put(String key, Object value);
  void evict(String key);
  void clear();
}
