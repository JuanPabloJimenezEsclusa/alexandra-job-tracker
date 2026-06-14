package com.jobtracker.server;

import com.jobtracker.auth.JjwtRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot application entry point.
 */
@SpringBootApplication(scanBasePackages = {
  "com.jobtracker.api",
  "com.jobtracker.auth",
  "com.jobtracker.persistence",
  "com.jobtracker.ai",
  "com.jobtracker.cache",
  "com.jobtracker.observability",
  "com.jobtracker.server"
})
@EnableJpaRepositories("com.jobtracker.persistence.repository")
@EntityScan("com.jobtracker.persistence.entity")
@ImportRuntimeHints(JjwtRuntimeHints.class)
public class JobTrackerServerApplication {
  static void main(String[] args) {
    SpringApplication.run(JobTrackerServerApplication.class, args);
  }
}
