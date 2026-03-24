package com.linkedin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ExperienceRequest {

    @NotBlank(message = "Please provide a valid title")
    private String title;

    @NotBlank(message = "Please provide a valid company")
    private String company;

    private String location;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isCurrent = false;

    private String description;
}
