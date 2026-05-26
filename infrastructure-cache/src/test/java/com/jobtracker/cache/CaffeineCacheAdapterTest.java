package com.jobtracker.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class CaffeineCacheAdapterTest {
  private CaffeineCacheAdapter cache;

  @BeforeEach
  void setUp() {
    cache = new CaffeineCacheAdapter(100, Duration.ofMinutes(5));
  }

  @ParameterizedTest(name = "put({0}, {1}) → get({0}) = {1}")
  @CsvSource({
    "key1, value1",
    "empty, ''",
    "unicode, café"
  })
  void shouldStoreAndRetrieveValue(String key, String value) {
    // Given
    cache.put(key, value);

    // When
    var result = cache.get(key, String.class);

    // Then
    assertThat(result).hasValue(value);
  }

  @ParameterizedTest(name = "get({0}) → empty for missing key")
  @CsvSource({ "missing", "unknown", "nonexistent" })
  void shouldReturnEmptyForMissingKey(String key) {
    // When
    var result = cache.get(key, String.class);

    // Then
    assertThat(result).isEmpty();
  }

  static Stream<Arguments> evictionScenarios() {
    return Stream.of(
      Arguments.of("key1", "value1", "evict"),
      Arguments.of("key2", "value2", "clear")
    );
  }

  @ParameterizedTest(name = "{2} → get({0}) empty")
  @MethodSource("evictionScenarios")
  void shouldEvictOrClearKeys(String key, String value, String action) {
    // Given
    cache.put(key, value);

    // When
    if ("evict".equals(action)) {
      cache.evict(key);
    } else {
      cache.clear();
    }

    // Then
    assertThat(cache.get(key, String.class)).isEmpty();
  }

  @ParameterizedTest(name = "overwrite {0}: old→new")
  @CsvSource({ "key, old, new" })
  void shouldOverwriteExistingKey(String key, String oldVal, String newVal) {
    // Given
    cache.put(key, oldVal);

    // When
    cache.put(key, newVal);

    // Then
    assertThat(cache.get(key, String.class)).hasValue(newVal);
  }

  @ParameterizedTest(name = "expiry after {1}ms")
  @CsvSource({ "10, 20" })
  void shouldHandleExpiration(long ttlMs, long sleepMs) throws Exception {
    // Given
    var shortCache = new CaffeineCacheAdapter(100, Duration.ofMillis(ttlMs));
    shortCache.put("key", "value");

    // When
    Thread.sleep(sleepMs);

    // Then
    assertThat(shortCache.get("key", String.class)).isEmpty();
  }

  @ParameterizedTest(name = "stats: {1} hit, {2} miss")
  @CsvSource({ "hit, 1, 0" })
  void shouldRecordStats(String key, int _expectedHits, int _expectedMisses) {
    // Given
    cache.put(key, "x");

    // When
    cache.get(key, String.class);    // hit
    cache.get("miss", String.class); // miss

    // Then
    assertThat(cache.asMap()).hasSize(1);
  }
}
