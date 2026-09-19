package dev.jpje.jobtracker.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

class PortPlacementGuardTest {

  @Test
  void shouldMoveTheCacheContractIntoTheCacheAdapter() throws ClassNotFoundException {
    assertThatThrownBy(() -> Class.forName("dev.jpje.jobtracker.domain.port.outbound.CachePort"))
      .isInstanceOf(ClassNotFoundException.class);
    final var cachePort = Class.forName("dev.jpje.jobtracker.cache.port.CachePort");
    assertThat(cachePort.isInterface()).as("relocated cache port is an interface").isTrue();
    assertThat(cachePort.getDeclaredMethods())
      .extracting(Method::getName)
      .containsExactlyInAnyOrder("get", "put", "evict", "clear");
  }
}
