package dev.jpje.jobtracker.persistence.repository;

import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.persistence.entity.JobPostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingJpaRepository extends JpaRepository<JobPostingEntity, UUID> {
  List<JobPostingEntity> findByUserId(UUID userId);
}
