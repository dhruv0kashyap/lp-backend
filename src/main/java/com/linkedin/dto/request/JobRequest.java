package com.linkedin.dto.request;

import com.linkedin.enums.ExperienceLevel;
import com.linkedin.enums.JobType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobRequest {

    @NotBlank(message = "Please provide a valid title")
    private String title;

    @NotBlank(message = "Please provide a valid company")
    private String company;

    @NotBlank(message = "Please provide a valid location")
    private String location;

    @NotNull(message = "Please provide a valid jobType")
    private JobType jobType;

    private ExperienceLevel experienceLevel = ExperienceLevel.ENTRY;

    @NotBlank(message = "Please provide a valid description")
    private String description;

    private String requirements;

    private String benefits;

    @Future(message = "Application deadline must be a future date")
    private LocalDate applicationDeadline;
}
