package com.jobtracker.persistence.repository;

import java.util.List;
import java.util.UUID;

import com.jobtracker.persistence.entity.JobApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationJpaRepository extends JpaRepository<JobApplicationEntity, UUID> {
  List<JobApplicationEntity> findByUserIdOrderByDateAppliedDesc(UUID userId);
}
