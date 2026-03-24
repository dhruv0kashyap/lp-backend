package com.linkedin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EducationRequest {

    @NotBlank(message = "Please provide a valid school")
    private String school;

    private String degree;

    private String fieldOfStudy;

    @Positive(message = "Please provide a valid startYear")
    private Integer startYear;

    @Positive(message = "Please provide a valid endYear")
    private Integer endYear;

    private String description;
}
