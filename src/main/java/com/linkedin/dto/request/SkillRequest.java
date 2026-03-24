package com.linkedin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SkillRequest {

    @NotBlank(message = "Please provide a valid skillName")
    private String skillName;
}
