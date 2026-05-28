package dev.jpje.jobtracker.server;

import dev.jpje.jobtracker.auth.JjwtRuntimeHints;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
  "dev.jpje.jobtracker.api",
  "dev.jpje.jobtracker.auth",
  "dev.jpje.jobtracker.persistence",
  "dev.jpje.jobtracker.ai",
  "dev.jpje.jobtracker.cache",
  "dev.jpje.jobtracker.observability",
  "dev.jpje.jobtracker.server"
})
@EnableJpaRepositories("dev.jpje.jobtracker.persistence.repository")
@EntityScan("dev.jpje.jobtracker.persistence.entity")
@ImportRuntimeHints(JjwtRuntimeHints.class)
public class JobTrackerServerApplication {
  static void main(String[] args) {
    SpringApplication.run(JobTrackerServerApplication.class, args);
  }
}
