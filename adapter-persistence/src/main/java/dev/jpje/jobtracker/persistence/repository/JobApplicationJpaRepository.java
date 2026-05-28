package dev.jpje.jobtracker.persistence.repository;

import java.util.List;
import java.util.UUID;

import dev.jpje.jobtracker.persistence.entity.JobApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationJpaRepository extends JpaRepository<JobApplicationEntity, UUID> {
  List<JobApplicationEntity> findByUserIdOrderByDateAppliedDesc(UUID userId);

  List<JobApplicationEntity> findByUserIdAndStatusOrderByDateAppliedDesc(UUID userId, String status);

  List<JobApplicationEntity> findByUserIdAndSourceOrderByDateAppliedDesc(UUID userId, String source);

  List<JobApplicationEntity> findByUserIdAndStatusAndSourceOrderByDateAppliedDesc(
    UUID userId, String status, String source);
}
