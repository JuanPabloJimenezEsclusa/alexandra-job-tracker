package com.jobtracker.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.jobtracker.persistence.entity.JobPostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for job postings.
 */
public interface JobPostingJpaRepository extends JpaRepository<JobPostingEntity, UUID> {
  /**
   * Finds all postings for a user.
   */
  List<JobPostingEntity> findByUserId(UUID userId);
}
