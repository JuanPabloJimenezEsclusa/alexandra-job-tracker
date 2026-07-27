package dev.jpje.jobtracker.server;

import dev.jpje.jobtracker.cache.CachingJobApplicationAdapter;
import dev.jpje.jobtracker.cache.CachingJobPostingAdapter;
import dev.jpje.jobtracker.domain.port.out.CachePort;
import dev.jpje.jobtracker.persistence.adapter.JobApplicationPersistenceAdapter;
import dev.jpje.jobtracker.persistence.adapter.JobPostingPersistenceAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
