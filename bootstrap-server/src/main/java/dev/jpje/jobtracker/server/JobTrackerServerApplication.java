package dev.jpje.jobtracker.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
  "dev.jpje.jobtracker.api",
  "dev.jpje.jobtracker.auth",
  "dev.jpje.jobtracker.persistence",
  "dev.jpje.jobtracker.ai",
  "dev.jpje.jobtracker.cache",
  "dev.jpje.jobtracker.events",
  "dev.jpje.jobtracker.observability",
  "dev.jpje.jobtracker.server"
})
public class JobTrackerServerApplication {
  static void main(String[] args) {
    SpringApplication.run(JobTrackerServerApplication.class, args);
  }
}
