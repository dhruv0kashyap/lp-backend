package com.linkedin.repository;

import com.linkedin.entity.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByUserIdOrderBySavedAtDesc(Long userId);
    Optional<SavedJob> findByJobIdAndUserId(Long jobId, Long userId);
    boolean existsByJobIdAndUserId(Long jobId, Long userId);
}
