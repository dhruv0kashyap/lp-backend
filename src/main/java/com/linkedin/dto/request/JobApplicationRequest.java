package com.linkedin.dto.request;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobApplicationRequest {
    private String resumeUrl;
    private String coverLetter;
}
