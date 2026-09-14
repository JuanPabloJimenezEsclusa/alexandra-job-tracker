package dev.jpje.jobtracker.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.persistence.entity.JobApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationJpaRepository extends JpaRepository<JobApplicationEntity, UUID> {
  Optional<JobApplicationEntity> findByIdAndUserId(UUID id, UUID userId);

  List<JobApplicationEntity> findByUserIdOrderByDateAppliedDesc(UUID userId);

  List<JobApplicationEntity> findByUserIdAndStatusOrderByDateAppliedDesc(UUID userId, String status);
}
