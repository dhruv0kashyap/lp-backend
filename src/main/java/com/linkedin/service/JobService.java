package com.linkedin.service;

import com.linkedin.dto.request.JobApplicationRequest;
import com.linkedin.dto.request.JobRequest;
import com.linkedin.dto.request.UpdateApplicationStatusRequest;
import com.linkedin.dto.response.JobApplicationResponse;
import com.linkedin.dto.response.JobResponse;
import com.linkedin.enums.ExperienceLevel;
import com.linkedin.enums.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {
    JobResponse createJob(String email, JobRequest request);
    JobResponse updateJob(String email, Long jobId, JobRequest request);
    void deleteJob(String email, Long jobId);
    JobResponse getJobById(String email, Long jobId);
    Page<JobResponse> getAllJobs(String email, Pageable pageable);
    Page<JobResponse> getMyPostedJobs(String email, Pageable pageable);
    Page<JobResponse> searchJobs(String email, String keyword, JobType jobType, ExperienceLevel experienceLevel, String location, Pageable pageable);
    JobApplicationResponse applyForJob(String email, Long jobId, JobApplicationRequest request);
    void withdrawApplication(String email, Long applicationId);
    Page<JobApplicationResponse> getMyApplications(String email, Pageable pageable);
    String saveJob(String email, Long jobId);
    Page<JobResponse> getSavedJobs(String email, Pageable pageable);
    JobApplicationResponse updateApplicationStatus(String email, Long applicationId, UpdateApplicationStatusRequest request);
    Page<JobApplicationResponse> getJobApplications(String email, Long jobId, Pageable pageable);
}
