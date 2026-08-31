package dev.jpje.jobtracker.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.time.Duration;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CaffeineCacheAdapterTest {

  private static final int MAX_SIZE = 100;
  private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
  private static final Duration SHORT_TTL = Duration.ofMillis(10);

  private static Stream<Arguments> valueScenarios() {
    return Stream.of(
      arguments(named("plain value", "key1"), "value1"),
      arguments(named("empty value", "empty"), ""),
      arguments(named("unicode value", "unicode"), "café")
    );
  }

  private static Stream<Arguments> missingKeyScenarios() {
    return Stream.of(
      arguments(named("missing key", "missing")),
      arguments(named("unknown key", "unknown")),
      arguments(named("nonexistent key", "nonexistent"))
    );
  }

  private static Stream<Arguments> keyValueScenarios() {
    return Stream.of(
      arguments(named("first pair", "key1"), "value1"),
      arguments(named("second pair", "key2"), "value2")
    );
  }

  @ParameterizedTest(name = "{0}: put → get returns stored value")
  @MethodSource("valueScenarios")
  void shouldStoreAndRetrieveValue(final String key, final String value) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);

    cache.put(key, value);

    assertThat(cache.get(key, String.class)).hasValue(value);
  }

  @ParameterizedTest(name = "{0}: get → empty")
  @MethodSource("missingKeyScenarios")
  void shouldReturnEmptyForMissingKey(final String key) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);

    assertThat(cache.get(key, String.class)).isEmpty();
  }

  @ParameterizedTest(name = "{0}: evict → get empty")
  @MethodSource("keyValueScenarios")
  void shouldEvictKeys(final String key, final String value) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);
    cache.put(key, value);

    cache.evict(key);

    assertThat(cache.get(key, String.class)).isEmpty();
  }

  @ParameterizedTest(name = "{0}: clear → get empty")
  @MethodSource("keyValueScenarios")
  void shouldClearAllKeys(final String key, final String value) {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);
    cache.put(key, value);

    cache.clear();

    assertThat(cache.get(key, String.class)).isEmpty();
  }

  @Test
  void shouldOverwriteExistingKey() {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, DEFAULT_TTL);
    cache.put("key", "old");

    cache.put("key", "new");

    assertThat(cache.get("key", String.class)).hasValue("new");
  }

  @Test
  void shouldHandleExpiration() {
    final var cache = new CaffeineCacheAdapter(MAX_SIZE, SHORT_TTL);
    cache.put("key", "value");

    await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
      assertThat(cache.get("key", String.class)).isEmpty());
  }
}
