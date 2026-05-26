package com.jobtracker.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
  "com.jobtracker.api",
  "com.jobtracker.auth",
  "com.jobtracker.persistence",
  "com.jobtracker.scraping",
  "com.jobtracker.ai",
  "com.jobtracker.observability"
})
@EnableJpaRepositories("com.jobtracker.persistence.repository")
@EntityScan("com.jobtracker.persistence.entity")
public class JobTrackerServerApplication {
  static void main(String[] args) {
    SpringApplication.run(JobTrackerServerApplication.class, args);
  }
}
