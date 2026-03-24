package com.linkedin.dto.response;

import com.linkedin.enums.ExperienceLevel;
import com.linkedin.enums.JobType;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobResponse {
    private Long id;
    private String title;
    private String company;
    private String location;
    private JobType jobType;
    private ExperienceLevel experienceLevel;
    private String description;
    private String requirements;
    private String benefits;
    private LocalDate applicationDeadline;
    private Long postedById;
    private String postedByName;
    private Boolean isActive;
    private boolean savedByCurrentUser;
    private boolean appliedByCurrentUser;
    private LocalDateTime createdAt;
}
