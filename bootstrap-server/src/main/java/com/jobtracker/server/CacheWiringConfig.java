package com.jobtracker.server;

import com.jobtracker.cache.CachingJobApplicationAdapter;
import com.jobtracker.cache.CachingJobPostingAdapter;
import com.jobtracker.domain.port.out.CachePort;
import com.jobtracker.persistence.adapter.JobApplicationPersistenceAdapter;
import com.jobtracker.persistence.adapter.JobPostingPersistenceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Wires caching decorators around persistence adapters.
 */
@Configuration(proxyBeanMethods = false)
public class CacheWiringConfig {

  @Bean
  @Primary
  CachingJobApplicationAdapter cachingJobApplicationAdapter(
    final JobApplicationPersistenceAdapter delegate,
    final CachePort cache
  ) {
    return new CachingJobApplicationAdapter(delegate, delegate, cache);
  }

  @Bean
  @Primary
  CachingJobPostingAdapter cachingJobPostingAdapter(
    final JobPostingPersistenceAdapter delegate,
    final CachePort cache
  ) {
    return new CachingJobPostingAdapter(delegate, delegate, cache);
  }
}
