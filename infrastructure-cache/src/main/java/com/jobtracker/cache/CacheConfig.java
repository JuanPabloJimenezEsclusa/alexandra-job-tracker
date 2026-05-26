package com.jobtracker.cache;

import java.time.Duration;

import com.jobtracker.domain.port.out.CachePort;
import org.springframework.beans.factory.annotation.Value;
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
}
