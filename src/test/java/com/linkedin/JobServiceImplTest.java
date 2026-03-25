
package com.linkedin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.linkedin.dto.request.JobApplicationRequest;
import com.linkedin.dto.request.JobRequest;
import com.linkedin.dto.request.UpdateApplicationStatusRequest;
import com.linkedin.dto.response.JobApplicationResponse;
import com.linkedin.dto.response.JobResponse;
import com.linkedin.entity.Job;
import com.linkedin.entity.JobApplication;
import com.linkedin.entity.Notification;
import com.linkedin.entity.SavedJob;
import com.linkedin.entity.User;
import com.linkedin.enums.ApplicationStatus;
import com.linkedin.enums.ExperienceLevel;
import com.linkedin.enums.JobType;
import com.linkedin.enums.NotificationType;
import com.linkedin.exception.BadRequestException;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.JobApplicationRepository;
import com.linkedin.repository.JobRepository;
import com.linkedin.repository.NotificationRepository;
import com.linkedin.repository.SavedJobRepository;
import com.linkedin.repository.UserRepository;
import com.linkedin.serviceImpl.JobServiceImpl;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

@Mock
private JobRepository jobRepository;
@Mock
private JobApplicationRepository jobApplicationRepository;
@Mock
private SavedJobRepository savedJobRepository;
@Mock
private UserRepository userRepository;
@Mock
private NotificationRepository notificationRepository;

@InjectMocks
private JobServiceImpl jobService;

private User buildUser(Long id, String email, String firstName, String lastName) {
    return User.builder()
            .id(id)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .build();
}

private Job buildJob(Long id, User postedBy) {
    return Job.builder()
            .id(id)
            .title("Software Engineer")
            .company("LinkedIn")
            .location("Bangalore")
            .jobType(JobType.FULL_TIME)
            .experienceLevel(ExperienceLevel.ENTRY)
            .description("Job description")
            .requirements("Java, Spring Boot")
            .benefits("Health insurance")
            .applicationDeadline(LocalDate.now().plusDays(10))
            .postedBy(postedBy)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build();
}

private JobApplication buildApplication(Long id, Job job, User user, ApplicationStatus status) {
    return JobApplication.builder()
            .id(id)
            .job(job)
            .user(user)
            .resumeUrl("resume.pdf")
            .coverLetter("cover letter")
            .status(status)
            .appliedAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
}

private JobRequest buildJobRequest() {
    return JobRequest.builder()
            .title("Software Engineer")
            .company("LinkedIn")
            .location("Bangalore")
            .jobType(JobType.FULL_TIME)
            .experienceLevel(ExperienceLevel.ENTRY)
            .description("Job description")
            .requirements("Java, Spring Boot")
            .benefits("Health insurance")
            .applicationDeadline(LocalDate.now().plusDays(10))
            .build();
}

@Test
void createJob_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    JobRequest request = buildJobRequest();
    Job savedJob = buildJob(100L, user);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
    when(savedJobRepository.existsByJobIdAndUserId(100L, 1L)).thenReturn(false);
    when(jobApplicationRepository.existsByJobIdAndUserId(100L, 1L)).thenReturn(false);

    JobResponse response = jobService.createJob("john@example.com", request);

    assertNotNull(response);
    assertEquals(100L, response.getId());
    assertEquals("Software Engineer", response.getTitle());
    verify(jobRepository).save(any(Job.class));
}

@Test
void updateJob_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    JobRequest request = buildJobRequest();
    Job job = buildJob(10L, user);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
    when(jobRepository.save(any(Job.class))).thenReturn(job);
    when(savedJobRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);
    when(jobApplicationRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);

    JobResponse response = jobService.updateJob("john@example.com", 10L, request);

    assertNotNull(response);
    assertEquals(10L, response.getId());
    verify(jobRepository).save(job);
}

@Test
void updateJob_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com", "John", "Doe");
    User owner = buildUser(2L, "owner@example.com", "Owner", "User");
    Job job = buildJob(10L, owner);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

    assertThrows(UnauthorizedException.class,
            () -> jobService.updateJob("john@example.com", 10L, buildJobRequest()));
}

@Test
void deleteJob_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Job job = buildJob(10L, user);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

    jobService.deleteJob("john@example.com", 10L);

    verify(jobRepository).delete(job);
}

@Test
void deleteJob_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com", "John", "Doe");
    User owner = buildUser(2L, "owner@example.com", "Owner", "User");
    Job job = buildJob(10L, owner);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

    assertThrows(UnauthorizedException.class,
            () -> jobService.deleteJob("john@example.com", 10L));
}

@Test
void getJobById_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Job job = buildJob(10L, user);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
    when(savedJobRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);
    when(jobApplicationRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);

    JobResponse response = jobService.getJobById("john@example.com", 10L);

    assertNotNull(response);
    assertEquals(10L, response.getId());
}

@Test
void getAllJobs_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Job job = buildJob(10L, user);
    Pageable pageable = PageRequest.of(0, 10);
    Page<Job> page = new PageImpl<>(List.of(job), pageable, 1);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable)).thenReturn(page);
    when(savedJobRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);
    when(jobApplicationRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);

    Page<JobResponse> response = jobService.getAllJobs("john@example.com", pageable);

    assertEquals(1, response.getContent().size());
}

@Test
void getMyPostedJobs_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Job job = buildJob(10L, user);
    Pageable pageable = PageRequest.of(0, 10);
    Page<Job> page = new PageImpl<>(List.of(job), pageable, 1);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.findByPostedByIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);
    when(savedJobRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);
    when(jobApplicationRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);

    Page<JobResponse> response = jobService.getMyPostedJobs("john@example.com", pageable);

    assertEquals(1, response.getContent().size());
}

@Test
void searchJobs_Success_WithKeywordAndLocationCleanup() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Job job = buildJob(10L, user);
    Pageable pageable = PageRequest.of(0, 10);
    Page<Job> page = new PageImpl<>(List.of(job), pageable, 1);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.searchJobs("java", JobType.FULL_TIME, ExperienceLevel.ENTRY, "Bangalore", pageable))
            .thenReturn(page);
    when(savedJobRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);
    when(jobApplicationRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);

    Page<JobResponse> response = jobService.searchJobs(
            "john@example.com", "java", JobType.FULL_TIME, ExperienceLevel.ENTRY, "Bangalore", pageable);

    assertEquals(1, response.getContent().size());
}

@Test
void searchJobs_BlankKeywordAndLocation_PassesNull() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Pageable pageable = PageRequest.of(0, 10);
    Page<Job> page = new PageImpl<>(List.of(), pageable, 0);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.searchJobs(null, null, null, null, pageable)).thenReturn(page);

    Page<JobResponse> response = jobService.searchJobs(
            "john@example.com", "   ", null, null, "   ", pageable);

    assertNotNull(response);
    verify(jobRepository).searchJobs(null, null, null, null, pageable);
}

@Test
void applyForJob_Success() {
    User applicant = buildUser(1L, "john@example.com", "John", "Doe");
    User poster = buildUser(2L, "poster@example.com", "Poster", "User");
    Job job = buildJob(10L, poster);

    JobApplicationRequest request = JobApplicationRequest.builder()
            .resumeUrl("resume.pdf")
            .coverLetter("I am interested")
            .build();

    JobApplication savedApplication = buildApplication(20L, job, applicant, ApplicationStatus.APPLIED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(applicant));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
    when(jobApplicationRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);
    when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(savedApplication);

    JobApplicationResponse response = jobService.applyForJob("john@example.com", 10L, request);

    assertNotNull(response);
    assertEquals(20L, response.getId());
    assertEquals(ApplicationStatus.APPLIED, response.getStatus());

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());
    assertEquals(NotificationType.JOB_APPLICATION_UPDATE, captor.getValue().getType());
}

@Test
void applyForJob_AlreadyApplied_ThrowsBadRequest() {
    User applicant = buildUser(1L, "john@example.com", "John", "Doe");
    User poster = buildUser(2L, "poster@example.com", "Poster", "User");
    Job job = buildJob(10L, poster);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(applicant));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
    when(jobApplicationRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(true);

    JobApplicationRequest request = JobApplicationRequest.builder()
            .resumeUrl("resume.pdf")
            .coverLetter("I am interested")
            .build();

    assertThrows(BadRequestException.class,
            () -> jobService.applyForJob("john@example.com", 10L, request));
}

@Test
void withdrawApplication_Success() {
    User applicant = buildUser(1L, "john@example.com", "John", "Doe");
    User poster = buildUser(2L, "poster@example.com", "Poster", "User");
    Job job = buildJob(10L, poster);
    JobApplication application = buildApplication(20L, job, applicant, ApplicationStatus.APPLIED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(applicant));
    when(jobApplicationRepository.findById(20L)).thenReturn(Optional.of(application));

    jobService.withdrawApplication("john@example.com", 20L);

    assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
    verify(jobApplicationRepository).save(application);
}

@Test
void withdrawApplication_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com", "John", "Doe");
    User actualApplicant = buildUser(2L, "jane@example.com", "Jane", "Doe");
    User poster = buildUser(3L, "poster@example.com", "Poster", "User");
    Job job = buildJob(10L, poster);
    JobApplication application = buildApplication(20L, job, actualApplicant, ApplicationStatus.APPLIED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(jobApplicationRepository.findById(20L)).thenReturn(Optional.of(application));

    assertThrows(UnauthorizedException.class,
            () -> jobService.withdrawApplication("john@example.com", 20L));
}

@Test
void getMyApplications_Success() {
    User applicant = buildUser(1L, "john@example.com", "John", "Doe");
    User poster = buildUser(2L, "poster@example.com", "Poster", "User");
    Job job = buildJob(10L, poster);
    JobApplication application = buildApplication(20L, job, applicant, ApplicationStatus.APPLIED);
    Pageable pageable = PageRequest.of(0, 10);
    Page<JobApplication> page = new PageImpl<>(List.of(application), pageable, 1);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(applicant));
    when(jobApplicationRepository.findByUserIdOrderByAppliedAtDesc(1L, pageable)).thenReturn(page);

    Page<JobApplicationResponse> response = jobService.getMyApplications("john@example.com", pageable);

    assertEquals(1, response.getContent().size());
}

@Test
void saveJob_WhenNotSaved_AddsJob() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    User poster = buildUser(2L, "poster@example.com", "Poster", "User");
    Job job = buildJob(10L, poster);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
    when(savedJobRepository.findByJobIdAndUserId(10L, 1L)).thenReturn(Optional.empty());

    String response = jobService.saveJob("john@example.com", 10L);

    assertEquals("Successfully added", response);
    verify(savedJobRepository).save(any(SavedJob.class));
}

@Test
void saveJob_WhenAlreadySaved_RemovesJob() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    User poster = buildUser(2L, "poster@example.com", "Poster", "User");
    Job job = buildJob(10L, poster);
    SavedJob savedJob = SavedJob.builder().id(50L).job(job).user(user).build();

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
    when(savedJobRepository.findByJobIdAndUserId(10L, 1L)).thenReturn(Optional.of(savedJob));

    String response = jobService.saveJob("john@example.com", 10L);

    assertEquals("Job removed from saved list", response);
    verify(savedJobRepository).delete(savedJob);
}

@Test
void getSavedJobs_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    User poster = buildUser(2L, "poster@example.com", "Poster", "User");
    Job job = buildJob(10L, poster);
    SavedJob savedJob = SavedJob.builder().id(50L).job(job).user(user).build();
    Pageable pageable = PageRequest.of(0, 10);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(savedJobRepository.findByUserIdOrderBySavedAtDesc(1L)).thenReturn(List.of(savedJob));
    when(jobRepository.findAllById(List.of(10L))).thenReturn(List.of(job));
    when(savedJobRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(true);
    when(jobApplicationRepository.existsByJobIdAndUserId(10L, 1L)).thenReturn(false);

    Page<JobResponse> response = jobService.getSavedJobs("john@example.com", pageable);

    assertEquals(1, response.getContent().size());
    assertTrue(response.getContent().get(0).isSavedByCurrentUser());
}

@Test
void updateApplicationStatus_Success_Interview() {
    User poster = buildUser(1L, "poster@example.com", "Poster", "User");
    User applicant = buildUser(2L, "john@example.com", "John", "Doe");
    Job job = buildJob(10L, poster);
    JobApplication application = buildApplication(20L, job, applicant, ApplicationStatus.APPLIED);

    UpdateApplicationStatusRequest request = UpdateApplicationStatusRequest.builder()
            .status(ApplicationStatus.INTERVIEW)
            .build();

    when(userRepository.findByEmail("poster@example.com")).thenReturn(Optional.of(poster));
    when(jobApplicationRepository.findById(20L)).thenReturn(Optional.of(application));
    when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(application);

    JobApplicationResponse response = jobService.updateApplicationStatus("poster@example.com", 20L, request);

    assertNotNull(response);
    assertEquals(ApplicationStatus.INTERVIEW, application.getStatus());

    ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(captor.capture());
    assertTrue(captor.getValue().getMessage().contains("interview"));
}

@Test
void updateApplicationStatus_Unauthorized_ThrowsException() {
    User otherUser = buildUser(1L, "other@example.com", "Other", "User");
    User poster = buildUser(2L, "poster@example.com", "Poster", "User");
    User applicant = buildUser(3L, "john@example.com", "John", "Doe");
    Job job = buildJob(10L, poster);
    JobApplication application = buildApplication(20L, job, applicant, ApplicationStatus.APPLIED);

    UpdateApplicationStatusRequest request = UpdateApplicationStatusRequest.builder()
            .status(ApplicationStatus.REVIEWING)
            .build();

    when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));
    when(jobApplicationRepository.findById(20L)).thenReturn(Optional.of(application));

    assertThrows(UnauthorizedException.class,
            () -> jobService.updateApplicationStatus("other@example.com", 20L, request));
}

@Test
void getJobApplications_Success() {
    User poster = buildUser(1L, "poster@example.com", "Poster", "User");
    User applicant = buildUser(2L, "john@example.com", "John", "Doe");
    Job job = buildJob(10L, poster);
    JobApplication application = buildApplication(20L, job, applicant, ApplicationStatus.APPLIED);

    Pageable pageable = PageRequest.of(0, 10);
    Page<JobApplication> page = new PageImpl<>(List.of(application), pageable, 1);

    when(userRepository.findByEmail("poster@example.com")).thenReturn(Optional.of(poster));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
    when(jobApplicationRepository.findByJobIdOrderByAppliedAtDesc(10L, pageable)).thenReturn(page);

    Page<JobApplicationResponse> response = jobService.getJobApplications("poster@example.com", 10L, pageable);

    assertEquals(1, response.getContent().size());
}

@Test
void getJobApplications_Unauthorized_ThrowsException() {
    User other = buildUser(1L, "other@example.com", "Other", "User");
    User poster = buildUser(2L, "poster@example.com", "Poster", "User");
    Job job = buildJob(10L, poster);

    Pageable pageable = PageRequest.of(0, 10);

    when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(other));
    when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

    assertThrows(UnauthorizedException.class,
            () -> jobService.getJobApplications("other@example.com", 10L, pageable));
}

@Test
void getUserByEmail_UserNotFound_ThrowsResourceNotFoundIndirectly() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> jobService.getAllJobs("missing@example.com", PageRequest.of(0, 10)));
}

}
