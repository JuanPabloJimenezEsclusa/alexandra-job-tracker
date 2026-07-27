package dev.jpje.jobtracker.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
  void shouldStoreAndRetrieveValue(final String key, final String value) {
    // Given
    cache.put(key, value);

    // When
    var result = cache.get(key, String.class);

    // Then
    assertThat(result).hasValue(value);
  }

  @ParameterizedTest(name = "get({0}) → empty for missing key")
  @CsvSource({"missing", "unknown", "nonexistent"})
  void shouldReturnEmptyForMissingKey(final String key) {
    // When
    var result = cache.get(key, String.class);

    // Then
    assertThat(result).isEmpty();
  }

  @ParameterizedTest(name = "evict({0}) → get({0}) empty")
  @CsvSource({"key1, value1", "key2, value2"})
  void shouldEvictKeys(final String key, final String value) {
    // Given
    cache.put(key, value);

    // When
    cache.evict(key);

    // Then
    assertThat(cache.get(key, String.class)).isEmpty();
  }

  @ParameterizedTest(name = "clear → get({0}) empty")
  @CsvSource({"key1, value1", "key2, value2"})
  void shouldClearAllKeys(final String key, final String value) {
    // Given
    cache.put(key, value);

    // When
    cache.clear();

    // Then
    assertThat(cache.get(key, String.class)).isEmpty();
  }

  @ParameterizedTest(name = "overwrite {0}: old→new")
  @CsvSource({"key, old, new"})
  void shouldOverwriteExistingKey(final String key, final String oldVal, final String newVal) {
    // Given
    cache.put(key, oldVal);

    // When
    cache.put(key, newVal);

    // Then
    assertThat(cache.get(key, String.class)).hasValue(newVal);
  }

  @ParameterizedTest(name = "expiry after {0}ms")
  @CsvSource("10")
  void shouldHandleExpiration(final long ttlMs) {
    // Given
    var shortCache = new CaffeineCacheAdapter(100, Duration.ofMillis(ttlMs));
    shortCache.put("key", "value");

    // When, then
    await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
      assertThat(shortCache.get("key", String.class)).isEmpty());
  }
}
