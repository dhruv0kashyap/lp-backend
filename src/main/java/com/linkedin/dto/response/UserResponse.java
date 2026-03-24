package com.linkedin.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profilePhotoUrl;
    private String headline;
    private String location;
    private String summary;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private List<EducationResponse> educations;
    private List<ExperienceResponse> experiences;
    private List<SkillResponse> skills;
}
