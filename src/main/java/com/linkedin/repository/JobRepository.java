package com.linkedin.repository;

import com.linkedin.entity.Job;
import com.linkedin.enums.ExperienceLevel;
import com.linkedin.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    // Fix: get jobs posted by a specific user
    Page<Job> findByPostedByIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE j.isActive = true AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(j.title) LIKE LOWER(CONCAT('%',:keyword,'%')) OR " +
           " LOWER(j.company) LIKE LOWER(CONCAT('%',:keyword,'%')) OR " +
           " LOWER(COALESCE(j.location,'')) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND " +
           "(:jobType IS NULL OR j.jobType = :jobType) AND " +
           "(:experienceLevel IS NULL OR j.experienceLevel = :experienceLevel) AND " +
           "(:location IS NULL OR :location = '' OR LOWER(COALESCE(j.location,'')) LIKE LOWER(CONCAT('%',:location,'%'))) " +
           "ORDER BY j.createdAt DESC")
    Page<Job> searchJobs(@Param("keyword") String keyword,
                         @Param("jobType") JobType jobType,
                         @Param("experienceLevel") ExperienceLevel experienceLevel,
                         @Param("location") String location,
                         Pageable pageable);
}
