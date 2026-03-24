package com.linkedin.repository;

import com.linkedin.entity.JobApplication;
import com.linkedin.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    Page<JobApplication> findByUserIdOrderByAppliedAtDesc(Long userId, Pageable pageable);
    Page<JobApplication> findByJobIdOrderByAppliedAtDesc(Long jobId, Pageable pageable);
    Optional<JobApplication> findByJobIdAndUserId(Long jobId, Long userId);
    boolean existsByJobIdAndUserId(Long jobId, Long userId);
}
