package dev.jpje.jobtracker.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.jpje.jobtracker.persistence.entity.JobAnalysisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobAnalysisJpaRepository extends JpaRepository<JobAnalysisEntity, UUID> {
  Optional<JobAnalysisEntity> findByIdAndUserId(UUID id, UUID userId);

  Optional<JobAnalysisEntity> findByJobPostingId(UUID jobPostingId);

  List<JobAnalysisEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
