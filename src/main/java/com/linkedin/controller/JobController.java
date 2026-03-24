package com.linkedin.controller;

import com.linkedin.dto.request.JobApplicationRequest;
import com.linkedin.dto.request.JobRequest;
import com.linkedin.dto.request.UpdateApplicationStatusRequest;
import com.linkedin.dto.response.*;
import com.linkedin.enums.ExperienceLevel;
import com.linkedin.enums.JobType;
import com.linkedin.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs", description = "Job listing and recruitment APIs")
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @AuthenticationPrincipal UserDetails u,
            @Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Job posted successfully",
                        jobService.createJob(u.getUsername(), request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @AuthenticationPrincipal UserDetails u,
            @PathVariable Long id, @Valid @RequestBody JobRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Job updated",
                jobService.updateJob(u.getUsername(), id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @AuthenticationPrincipal UserDetails u, @PathVariable Long id) {
        jobService.deleteJob(u.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Job deleted"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(
            @AuthenticationPrincipal UserDetails u, @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Job fetched",
                jobService.getJobById(u.getUsername(), id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getAllJobs(
            @AuthenticationPrincipal UserDetails u,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched",
                jobService.getAllJobs(u.getUsername(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/my-posted")
    @Operation(summary = "Get jobs posted by the current user")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getMyPostedJobs(
            @AuthenticationPrincipal UserDetails u,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Posted jobs fetched",
                jobService.getMyPostedJobs(u.getUsername(),
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> searchJobs(
            @AuthenticationPrincipal UserDetails u,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Jobs fetched",
                jobService.searchJobs(u.getUsername(), keyword, jobType,
                        experienceLevel, location, PageRequest.of(page, size))));
    }

    @PostMapping("/{id}/apply")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> applyForJob(
            @AuthenticationPrincipal UserDetails u,
            @PathVariable Long id,
            @RequestBody JobApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Application submitted successfully",
                        jobService.applyForJob(u.getUsername(), id, request)));
    }

    @PutMapping("/applications/{applicationId}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdrawApplication(
            @AuthenticationPrincipal UserDetails u,
            @PathVariable Long applicationId) {
        jobService.withdrawApplication(u.getUsername(), applicationId);
        return ResponseEntity.ok(ApiResponse.success("Application withdrawn"));
    }

    @GetMapping("/applications/my")
    public ResponseEntity<ApiResponse<Page<JobApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserDetails u,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                jobService.getMyApplications(u.getUsername(), PageRequest.of(page, size))));
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse<String>> saveJob(
            @AuthenticationPrincipal UserDetails u, @PathVariable Long id) {
        String msg = jobService.saveJob(u.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success(msg, msg));
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<Page<JobResponse>>> getSavedJobs(
            @AuthenticationPrincipal UserDetails u,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Saved jobs fetched",
                jobService.getSavedJobs(u.getUsername(), PageRequest.of(page, size))));
    }

    @GetMapping("/{id}/applications")
    @Operation(summary = "Get all applications for a job (job poster only)")
    public ResponseEntity<ApiResponse<Page<JobApplicationResponse>>> getJobApplications(
            @AuthenticationPrincipal UserDetails u,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                jobService.getJobApplications(u.getUsername(), id, PageRequest.of(page, size))));
    }

    @PatchMapping("/applications/{applicationId}/status")
    @Operation(summary = "Update application status (job poster only)")
    public ResponseEntity<ApiResponse<JobApplicationResponse>> updateApplicationStatus(
            @AuthenticationPrincipal UserDetails u,
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                jobService.updateApplicationStatus(u.getUsername(), applicationId, request)));
    }
}
