package com.linkedin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.linkedin.controller.JobController;
import com.linkedin.dto.request.JobApplicationRequest;
import com.linkedin.dto.request.JobRequest;
import com.linkedin.dto.request.UpdateApplicationStatusRequest;
import com.linkedin.dto.response.ApiResponse;
import com.linkedin.dto.response.JobApplicationResponse;
import com.linkedin.dto.response.JobResponse;
import com.linkedin.enums.ExperienceLevel;
import com.linkedin.enums.JobType;
import com.linkedin.service.JobService;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

@Mock
private JobService jobService;

@InjectMocks
private JobController jobController;

private UserDetails getUserDetails() {
    return User.withUsername("john.doe@example.com")
            .password("password")
            .authorities(Collections.emptyList())
            .build();
}

@Test
void createJob_Success() {
    UserDetails userDetails = getUserDetails();
    JobRequest request = new JobRequest();
    JobResponse jobResponse = new JobResponse();

    when(jobService.createJob("john.doe@example.com", request)).thenReturn(jobResponse);

    ResponseEntity<ApiResponse<JobResponse>> response =
            jobController.createJob(userDetails, request);

    assertEquals(201, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Job posted successfully", response.getBody().getMessage());
    assertEquals(jobResponse, response.getBody().getData());
}

@Test
void updateJob_Success() {
    UserDetails userDetails = getUserDetails();
    JobRequest request = new JobRequest();
    JobResponse jobResponse = new JobResponse();

    when(jobService.updateJob("john.doe@example.com", 1L, request)).thenReturn(jobResponse);

    ResponseEntity<ApiResponse<JobResponse>> response =
            jobController.updateJob(userDetails, 1L, request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Job updated", response.getBody().getMessage());
    assertEquals(jobResponse, response.getBody().getData());
}

@Test
void deleteJob_Success() {
    UserDetails userDetails = getUserDetails();

    doNothing().when(jobService).deleteJob("john.doe@example.com", 1L);

    ResponseEntity<ApiResponse<Void>> response =
            jobController.deleteJob(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Job deleted", response.getBody().getMessage());
    verify(jobService).deleteJob("john.doe@example.com", 1L);
}

@Test
void getJobById_Success() {
    UserDetails userDetails = getUserDetails();
    JobResponse jobResponse = new JobResponse();

    when(jobService.getJobById("john.doe@example.com", 1L)).thenReturn(jobResponse);

    ResponseEntity<ApiResponse<JobResponse>> response =
            jobController.getJobById(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Job fetched", response.getBody().getMessage());
    assertEquals(jobResponse, response.getBody().getData());
}

@Test
void getAllJobs_Success() {
    UserDetails userDetails = getUserDetails();
    Page<JobResponse> page = new PageImpl<>(List.of(new JobResponse()));

    when(jobService.getAllJobs(eq("john.doe@example.com"), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<JobResponse>>> response =
            jobController.getAllJobs(userDetails, 0, 10);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Jobs fetched", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void getMyPostedJobs_Success() {
    UserDetails userDetails = getUserDetails();
    Page<JobResponse> page = new PageImpl<>(List.of(new JobResponse()));

    when(jobService.getMyPostedJobs(eq("john.doe@example.com"), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<JobResponse>>> response =
            jobController.getMyPostedJobs(userDetails, 0, 20);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Posted jobs fetched", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void searchJobs_Success() {
    UserDetails userDetails = getUserDetails();
    Page<JobResponse> page = new PageImpl<>(List.of(new JobResponse()));

    when(jobService.searchJobs(eq("john.doe@example.com"), eq("java"), eq(JobType.FULL_TIME),
            eq(ExperienceLevel.ENTRY), eq("Bangalore"), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<JobResponse>>> response =
            jobController.searchJobs(userDetails, "java", JobType.FULL_TIME,
                    ExperienceLevel.ENTRY, "Bangalore", 0, 10);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Jobs fetched", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void applyForJob_Success() {
    UserDetails userDetails = getUserDetails();
    JobApplicationRequest request = new JobApplicationRequest();
    JobApplicationResponse applicationResponse = new JobApplicationResponse();

    when(jobService.applyForJob("john.doe@example.com", 1L, request)).thenReturn(applicationResponse);

    ResponseEntity<ApiResponse<JobApplicationResponse>> response =
            jobController.applyForJob(userDetails, 1L, request);

    assertEquals(201, response.getStatusCode().value());
    assertEquals("Application submitted successfully", response.getBody().getMessage());
    assertEquals(applicationResponse, response.getBody().getData());
}

@Test
void withdrawApplication_Success() {
    UserDetails userDetails = getUserDetails();

    doNothing().when(jobService).withdrawApplication("john.doe@example.com", 10L);

    ResponseEntity<ApiResponse<Void>> response =
            jobController.withdrawApplication(userDetails, 10L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Application withdrawn", response.getBody().getMessage());
    verify(jobService).withdrawApplication("john.doe@example.com", 10L);
}

@Test
void getMyApplications_Success() {
    UserDetails userDetails = getUserDetails();
    Page<JobApplicationResponse> page = new PageImpl<>(List.of(new JobApplicationResponse()));

    when(jobService.getMyApplications(eq("john.doe@example.com"), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<JobApplicationResponse>>> response =
            jobController.getMyApplications(userDetails, 0, 20);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Applications fetched", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void saveJob_Success() {
    UserDetails userDetails = getUserDetails();

    when(jobService.saveJob("john.doe@example.com", 5L)).thenReturn("Job saved successfully");

    ResponseEntity<ApiResponse<String>> response =
            jobController.saveJob(userDetails, 5L);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Job saved successfully", response.getBody().getMessage());
    assertEquals("Job saved successfully", response.getBody().getData());
}

@Test
void getSavedJobs_Success() {
    UserDetails userDetails = getUserDetails();
    Page<JobResponse> page = new PageImpl<>(List.of(new JobResponse()));

    when(jobService.getSavedJobs(eq("john.doe@example.com"), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<JobResponse>>> response =
            jobController.getSavedJobs(userDetails, 0, 10);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Saved jobs fetched", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void getJobApplications_Success() {
    UserDetails userDetails = getUserDetails();
    Page<JobApplicationResponse> page = new PageImpl<>(List.of(new JobApplicationResponse()));

    when(jobService.getJobApplications(eq("john.doe@example.com"), eq(1L), any())).thenReturn(page);

    ResponseEntity<ApiResponse<Page<JobApplicationResponse>>> response =
            jobController.getJobApplications(userDetails, 1L, 0, 50);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Applications fetched", response.getBody().getMessage());
    assertEquals(page, response.getBody().getData());
}

@Test
void updateApplicationStatus_Success() {
    UserDetails userDetails = getUserDetails();
    UpdateApplicationStatusRequest request = new UpdateApplicationStatusRequest();
    JobApplicationResponse applicationResponse = new JobApplicationResponse();

    when(jobService.updateApplicationStatus("john.doe@example.com", 10L, request))
            .thenReturn(applicationResponse);

    ResponseEntity<ApiResponse<JobApplicationResponse>> response =
            jobController.updateApplicationStatus(userDetails, 10L, request);

    assertEquals(200, response.getStatusCode().value());
    assertEquals("Status updated", response.getBody().getMessage());
    assertEquals(applicationResponse, response.getBody().getData());
}

}
