package dev.jpje.jobtracker.persistence.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@EnableJpaRepositories("dev.jpje.jobtracker.persistence.repository")
@EntityScan("dev.jpje.jobtracker.persistence.entity")
public class PersistenceAutoConfig {
}
