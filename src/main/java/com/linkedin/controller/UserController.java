package com.linkedin.controller;

import com.linkedin.dto.request.*;
import com.linkedin.dto.response.*;
import com.linkedin.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User profile and management APIs")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully",
                userService.getCurrentUser(userDetails.getUsername())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", userService.getUserById(id)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully",
                userService.updateProfile(userDetails.getUsername(), request)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false, defaultValue = "") String keyword) {
        List<UserResponse> results = userService.searchUsers(keyword)
                .stream()
                .filter(u -> !u.getEmail().equals(userDetails.getUsername()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", results));
    }

    // Education endpoints
    @PostMapping("/me/education")
    public ResponseEntity<ApiResponse<EducationResponse>> addEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Education added successfully",
                userService.addEducation(userDetails.getUsername(), request)));
    }

    @PutMapping("/me/education/{id}")
    public ResponseEntity<ApiResponse<EducationResponse>> updateEducation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody EducationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Education updated successfully",
                userService.updateEducation(userDetails.getUsername(), id, request)));
    }

    @DeleteMapping("/me/education/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEducation(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        userService.deleteEducation(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Education deleted successfully"));
    }

    // Experience endpoints
    @PostMapping("/me/experience")
    public ResponseEntity<ApiResponse<ExperienceResponse>> addExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Experience added successfully",
                userService.addExperience(userDetails.getUsername(), request)));
    }

    @PutMapping("/me/experience/{id}")
    public ResponseEntity<ApiResponse<ExperienceResponse>> updateExperience(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody ExperienceRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Experience updated successfully",
                userService.updateExperience(userDetails.getUsername(), id, request)));
    }

    @DeleteMapping("/me/experience/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExperience(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        userService.deleteExperience(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Experience deleted successfully"));
    }

    // Skill endpoints
    @PostMapping("/me/skills")
    public ResponseEntity<ApiResponse<SkillResponse>> addSkill(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Skill added successfully",
                userService.addSkill(userDetails.getUsername(), request)));
    }

    @DeleteMapping("/me/skills/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSkill(
            @AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        userService.deleteSkill(userDetails.getUsername(), id);
        return ResponseEntity.ok(ApiResponse.success("Skill deleted successfully"));
    }
}
