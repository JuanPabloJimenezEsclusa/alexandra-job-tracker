package dev.jpje.jobtracker.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "dev.jpje.jobtracker.cli")
public class JobTrackerCliApplication {
  static void main(String[] args) {
    SpringApplication.run(JobTrackerCliApplication.class, args);
  }
}
