package com.linkedin.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EducationResponse {
    private Long id;
    private Long userId;
    private String school;
    private String degree;
    private String fieldOfStudy;
    private Integer startYear;
    private Integer endYear;
    private String description;
    private LocalDateTime createdAt;
}
