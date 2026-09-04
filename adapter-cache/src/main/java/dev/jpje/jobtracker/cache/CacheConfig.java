package dev.jpje.jobtracker.cache;

import java.time.Duration;

import dev.jpje.jobtracker.domain.port.outbound.CachePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CacheConfig {

  @Bean
  CachePort cachePort(
    @Value("${cache.max-size:1000}") final int maxSize,
    @Value("${cache.default-ttl-seconds:300}") final int defaultTtlSeconds
  ) {
    return new CaffeineCacheAdapter(maxSize, Duration.ofSeconds(defaultTtlSeconds));
  }

  @Bean
  CacheManager cacheManager() {
    return new CaffeineCacheManager();
  }
}
