package com.jobtracker.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot application entry point for the CLI.
 */
@SpringBootApplication(scanBasePackages = "com.jobtracker.cli")
public class JobTrackerCliApplication {
  static void main(String[] args) {
    SpringApplication.run(JobTrackerCliApplication.class, args);
  }
}
