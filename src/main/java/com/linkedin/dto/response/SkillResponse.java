package com.linkedin.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillResponse {
    private Long id;
    private Long userId;
    private String skillName;
    private LocalDateTime createdAt;
}
