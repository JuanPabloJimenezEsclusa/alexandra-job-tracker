package com.jobtracker.persistence.repository;

import java.util.UUID;

import com.jobtracker.persistence.entity.JobPostingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingJpaRepository extends JpaRepository<JobPostingEntity, UUID> {
}
