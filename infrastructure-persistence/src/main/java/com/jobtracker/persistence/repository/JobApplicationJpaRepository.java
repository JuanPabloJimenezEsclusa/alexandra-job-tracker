package com.jobtracker.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.jobtracker.persistence.entity.JobApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for job applications.
 */
public interface JobApplicationJpaRepository extends JpaRepository<JobApplicationEntity, UUID> {
  /**
   * Finds all applications for a user, ordered by date applied descending.
   */
  List<JobApplicationEntity> findByUserIdOrderByDateAppliedDesc(UUID userId);
}
