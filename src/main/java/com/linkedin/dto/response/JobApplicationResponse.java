package com.linkedin.dto.response;

import com.linkedin.enums.ApplicationStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobApplicationResponse {
    private Long id;
    private Long jobId;
    private String jobTitle;
    private String company;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String resumeUrl;
    private String coverLetter;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
