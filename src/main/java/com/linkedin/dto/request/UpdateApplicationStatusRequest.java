package com.linkedin.dto.request;

import com.linkedin.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateApplicationStatusRequest {

    @NotNull(message = "Please provide a valid status")
    private ApplicationStatus status;
}
