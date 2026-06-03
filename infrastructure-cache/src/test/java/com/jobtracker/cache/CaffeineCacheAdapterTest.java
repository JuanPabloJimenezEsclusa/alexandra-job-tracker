package com.jobtracker.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class CaffeineCacheAdapterTest {
  private CaffeineCacheAdapter cache;

  private static Stream<Arguments> evictionScenarios() {
    return Stream.of(
      arguments("key1", "value1", "evict"),
      arguments("key2", "value2", "clear")
    );
  }

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

  @ParameterizedTest(name = "{2} → get({0}) empty")
  @MethodSource("evictionScenarios")
  void shouldEvictOrClearKeys(final String key, final String value, final String action) {
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
  @CsvSource({"key, old, new"})
  void shouldOverwriteExistingKey(final String key, final String oldVal, final String newVal) {
    // Given
    cache.put(key, oldVal);

    // When
    cache.put(key, newVal);

    // Then
    assertThat(cache.get(key, String.class)).hasValue(newVal);
  }

  @Test
  void shouldHandleExpiration() {
    // Given
    var shortCache = new CaffeineCacheAdapter(100, Duration.ofMillis(10));
    shortCache.put("key", "value");

    // When, then
    await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
      assertThat(shortCache.get("key", String.class)).isEmpty());
  }
}
