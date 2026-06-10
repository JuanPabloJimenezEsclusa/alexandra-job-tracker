package com.jobtracker.observability.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Micrometer metrics for monitoring.
 */
@Configuration
public class MetricsConfig {

  /**
   * Counter for total job applications created.
   */
  @Bean
  public Counter applicationCreatedCounter(final MeterRegistry registry) {
    return Counter.builder("jobtracker.applications.created")
      .description("Total applications created")
      .register(registry);
  }

  /**
   * Timer for job posting submission duration.
   */
  @Bean
  public Timer submitDurationTimer(final MeterRegistry registry) {
    return Timer.builder("jobtracker.submit.duration")
      .description("Time spent submitting job postings")
      .register(registry);
  }

  /**
   * Timer for AI job analysis duration.
   */
  @Bean
  public Timer analyzeJobDurationTimer(final MeterRegistry registry) {
    return Timer.builder("jobtracker.ai.analyze.duration")
      .description("Time spent analyzing job postings")
      .register(registry);
  }
}
