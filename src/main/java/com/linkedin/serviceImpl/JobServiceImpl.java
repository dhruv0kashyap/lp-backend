package com.linkedin.serviceImpl;

import com.linkedin.dto.request.JobApplicationRequest;
import com.linkedin.dto.request.JobRequest;
import com.linkedin.dto.request.UpdateApplicationStatusRequest;
import com.linkedin.dto.response.JobApplicationResponse;
import com.linkedin.dto.response.JobResponse;
import com.linkedin.entity.*;
import com.linkedin.enums.ApplicationStatus;
import com.linkedin.enums.ExperienceLevel;
import com.linkedin.enums.JobType;
import com.linkedin.enums.NotificationType;
import com.linkedin.exception.BadRequestException;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.*;
import com.linkedin.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final SavedJobRepository savedJobRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public JobResponse createJob(String email, JobRequest request) {
        User user = getUserByEmail(email);
        Job job = Job.builder()
                .title(request.getTitle()).company(request.getCompany()).location(request.getLocation())
                .jobType(request.getJobType()).experienceLevel(request.getExperienceLevel())
                .description(request.getDescription()).requirements(request.getRequirements())
                .benefits(request.getBenefits()).applicationDeadline(request.getApplicationDeadline())
                .postedBy(user).isActive(true).build();
        job = jobRepository.save(job);
        log.info("Job created: {} by {}", job.getTitle(), email);
        return mapToJobResponse(job, user.getId());
    }

    @Override
    @Transactional
    public JobResponse updateJob(String email, Long jobId, JobRequest request) {
        User user = getUserByEmail(email);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getPostedBy().getId().equals(user.getId()))
            throw new UnauthorizedException("You are not authorized to update this job");
        job.setTitle(request.getTitle()); job.setCompany(request.getCompany());
        job.setLocation(request.getLocation()); job.setJobType(request.getJobType());
        job.setExperienceLevel(request.getExperienceLevel()); job.setDescription(request.getDescription());
        job.setRequirements(request.getRequirements()); job.setBenefits(request.getBenefits());
        job.setApplicationDeadline(request.getApplicationDeadline());
        return mapToJobResponse(jobRepository.save(job), user.getId());
    }

    @Override
    @Transactional
    public void deleteJob(String email, Long jobId) {
        User user = getUserByEmail(email);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getPostedBy().getId().equals(user.getId()))
            throw new UnauthorizedException("You are not authorized to delete this job");
        jobRepository.delete(job);
    }

    @Override
    public JobResponse getJobById(String email, Long jobId) {
        User user = getUserByEmail(email);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return mapToJobResponse(job, user.getId());
    }

    @Override
    public Page<JobResponse> getAllJobs(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return jobRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)
                .map(j -> mapToJobResponse(j, user.getId()));
    }

    @Override
    public Page<JobResponse> getMyPostedJobs(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return jobRepository.findByPostedByIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(j -> mapToJobResponse(j, user.getId()));
    }

    @Override
    public Page<JobResponse> searchJobs(String email, String keyword, JobType jobType,
                                         ExperienceLevel experienceLevel, String location, Pageable pageable) {
        User user = getUserByEmail(email);
        return jobRepository.searchJobs(
                (keyword == null || keyword.isBlank()) ? null : keyword,
                jobType,
                experienceLevel,
                (location == null || location.isBlank()) ? null : location,
                pageable)
                .map(j -> mapToJobResponse(j, user.getId()));
    }

    @Override
    @Transactional
    public JobApplicationResponse applyForJob(String email, Long jobId, JobApplicationRequest request) {
        User user = getUserByEmail(email);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (jobApplicationRepository.existsByJobIdAndUserId(jobId, user.getId()))
            throw new BadRequestException("You have already applied for this job");

        JobApplication application = JobApplication.builder()
                .job(job).user(user).resumeUrl(request.getResumeUrl())
                .coverLetter(request.getCoverLetter()).status(ApplicationStatus.APPLIED).build();
        application = jobApplicationRepository.save(application);

        notificationRepository.save(Notification.builder()
                .user(job.getPostedBy())
                .type(NotificationType.JOB_APPLICATION_UPDATE)
                .message(user.getFirstName() + " " + user.getLastName() + " applied for: " + job.getTitle())
                .referenceId(jobId).isRead(false).build());

        return mapToApplicationResponse(application);
    }

    @Override
    @Transactional
    public void withdrawApplication(String email, Long applicationId) {
        User user = getUserByEmail(email);
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getUser().getId().equals(user.getId()))
            throw new UnauthorizedException("You are not authorized to withdraw this application");
        application.setStatus(ApplicationStatus.WITHDRAWN);
        jobApplicationRepository.save(application);
    }

    @Override
    public Page<JobApplicationResponse> getMyApplications(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return jobApplicationRepository.findByUserIdOrderByAppliedAtDesc(user.getId(), pageable)
                .map(this::mapToApplicationResponse);
    }

    @Override
    @Transactional
    public String saveJob(String email, Long jobId) {
        User user = getUserByEmail(email);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return savedJobRepository.findByJobIdAndUserId(jobId, user.getId())
                .map(saved -> { savedJobRepository.delete(saved); return "Job removed from saved list"; })
                .orElseGet(() -> {
                    savedJobRepository.save(SavedJob.builder().job(job).user(user).build());
                    return "Successfully added";
                });
    }

    @Override
    public Page<JobResponse> getSavedJobs(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        List<SavedJob> savedJobs = savedJobRepository.findByUserIdOrderBySavedAtDesc(user.getId());
        List<JobResponse> list = jobRepository.findAllById(
                savedJobs.stream().map(s -> s.getJob().getId()).toList())
                .stream().map(j -> mapToJobResponse(j, user.getId())).collect(Collectors.toList());
        return new PageImpl<>(list, pageable, list.size());
    }

    @Override
    @Transactional
    public JobApplicationResponse updateApplicationStatus(String email, Long applicationId, UpdateApplicationStatusRequest request) {
        User user = getUserByEmail(email);
        JobApplication application = jobApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getJob().getPostedBy().getId().equals(user.getId()))
            throw new UnauthorizedException("Only the job poster can update application status");

        application.setStatus(request.getStatus());
        application = jobApplicationRepository.save(application);

        String statusMsg = switch (request.getStatus()) {
            case REVIEWING -> "Your application is being reviewed";
            case INTERVIEW -> "🎉 You've been selected for an interview";
            case OFFERED   -> "🎉 Congratulations! You have a job offer";
            case REJECTED  -> "Your application was not selected this time";
            default        -> "Your application status updated to " + request.getStatus();
        };

        notificationRepository.save(Notification.builder()
                .user(application.getUser())
                .type(NotificationType.JOB_APPLICATION_UPDATE)
                .message(statusMsg + " for " + application.getJob().getTitle() + " at " + application.getJob().getCompany())
                .referenceId(application.getJob().getId()).isRead(false).build());

        return mapToApplicationResponse(application);
    }

    @Override
    public Page<JobApplicationResponse> getJobApplications(String email, Long jobId, Pageable pageable) {
        User user = getUserByEmail(email);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (!job.getPostedBy().getId().equals(user.getId()))
            throw new UnauthorizedException("Only the job poster can view applications");
        return jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(jobId, pageable)
                .map(this::mapToApplicationResponse);
    }

    private JobResponse mapToJobResponse(Job job, Long currentUserId) {
        return JobResponse.builder()
                .id(job.getId()).title(job.getTitle()).company(job.getCompany()).location(job.getLocation())
                .jobType(job.getJobType()).experienceLevel(job.getExperienceLevel())
                .description(job.getDescription()).requirements(job.getRequirements())
                .benefits(job.getBenefits()).applicationDeadline(job.getApplicationDeadline())
                .postedById(job.getPostedBy().getId())
                .postedByName(job.getPostedBy().getFirstName() + " " + job.getPostedBy().getLastName())
                .isActive(job.getIsActive())
                .savedByCurrentUser(savedJobRepository.existsByJobIdAndUserId(job.getId(), currentUserId))
                .appliedByCurrentUser(jobApplicationRepository.existsByJobIdAndUserId(job.getId(), currentUserId))
                .createdAt(job.getCreatedAt())
                .build();
    }

    private JobApplicationResponse mapToApplicationResponse(JobApplication app) {
        return JobApplicationResponse.builder()
                .id(app.getId()).jobId(app.getJob().getId()).jobTitle(app.getJob().getTitle())
                .company(app.getJob().getCompany()).userId(app.getUser().getId())
                .userFirstName(app.getUser().getFirstName()).userLastName(app.getUser().getLastName())
                .resumeUrl(app.getResumeUrl()).coverLetter(app.getCoverLetter())
                .status(app.getStatus()).appliedAt(app.getAppliedAt()).updatedAt(app.getUpdatedAt())
                .build();
    }
}
