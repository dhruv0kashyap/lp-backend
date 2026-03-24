package com.linkedin.service;

import com.linkedin.dto.request.*;
import com.linkedin.dto.response.*;
import java.util.List;

public interface UserService {
    UserResponse getCurrentUser(String email);
    UserResponse getUserById(Long id);
    UserResponse updateProfile(String email, ProfileUpdateRequest request);
    List<UserResponse> searchUsers(String keyword);

    EducationResponse addEducation(String email, EducationRequest request);
    EducationResponse updateEducation(String email, Long educationId, EducationRequest request);
    void deleteEducation(String email, Long educationId);

    ExperienceResponse addExperience(String email, ExperienceRequest request);
    ExperienceResponse updateExperience(String email, Long experienceId, ExperienceRequest request);
    void deleteExperience(String email, Long experienceId);

    SkillResponse addSkill(String email, SkillRequest request);
    void deleteSkill(String email, Long skillId);
}
