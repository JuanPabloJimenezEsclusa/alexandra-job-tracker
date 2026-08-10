package dev.jpje.jobtracker.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CaffeineCacheAdapterTest {

  private static final int MAX_SIZE = 100;
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);

  @ParameterizedTest(name = "put({0}, {1}) → get({0}) = {1}")
  @CsvSource({
    "key1, value1",
    "empty, ''",
    "unicode, café"
  })
  void shouldStoreAndRetrieveValue(final String key, final String value) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);

    cache.put(key, value);

    assertThat(cache.get(key, String.class)).hasValue(value);
  }

  @ParameterizedTest(name = "get({0}) → empty for missing key")
  @CsvSource({"missing", "unknown", "nonexistent"})
  void shouldReturnEmptyForMissingKey(final String key) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);

    assertThat(cache.get(key, String.class)).isEmpty();
  }

  @ParameterizedTest(name = "evict({0}) → get({0}) empty")
  @CsvSource({"key1, value1", "key2, value2"})
  void shouldEvictKeys(final String key, final String value) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);
    cache.put(key, value);

    cache.evict(key);

    assertThat(cache.get(key, String.class)).isEmpty();
  }

  @ParameterizedTest(name = "clear → get({0}) empty")
  @CsvSource({"key1, value1", "key2, value2"})
  void shouldClearAllKeys(final String key, final String value) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);
    cache.put(key, value);

    cache.clear();

    assertThat(cache.get(key, String.class)).isEmpty();
  }

  @ParameterizedTest(name = "overwrite {0}: old→new")
  @CsvSource({"key, old, new"})
  void shouldOverwriteExistingKey(final String key, final String oldVal, final String newVal) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);
    cache.put(key, oldVal);

    cache.put(key, newVal);

    assertThat(cache.get(key, String.class)).hasValue(newVal);
  }

  @ParameterizedTest(name = "expiry after {0}ms")
  @CsvSource("10")
  void shouldHandleExpiration(final long ttlMs) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, Duration.ofMillis(ttlMs));
    cache.put("key", "value");

    await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
      assertThat(cache.get("key", String.class)).isEmpty());
  }
}
