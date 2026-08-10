package dev.jpje.jobtracker.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@Profile("!aws")
public class AsyncConfig {

  @Bean
  TaskExecutor taskExecutor() {
    final var executor = new ThreadPoolTaskExecutor();
    executor.setVirtualThreads(true);
    executor.setThreadNamePrefix("ajt-async-");
    executor.initialize();
    return executor;
  }
}
