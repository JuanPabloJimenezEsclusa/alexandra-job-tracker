package com.jobtracker.server;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The type Time config.
 */
@Configuration(proxyBeanMethods = false)
public class TimeConfig {

  /**
   * Clock.
   */
  @Bean
  Clock clock() {
    return Clock.systemDefaultZone();
  }
}
