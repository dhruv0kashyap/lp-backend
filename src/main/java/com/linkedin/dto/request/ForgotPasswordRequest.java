package com.linkedin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ForgotPasswordRequest {

    @NotBlank(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Please provide a valid phoneNumber")
    @Pattern(regexp = "^[0-9]{10}$", message = "Please provide a valid phoneNumber")
    private String phoneNumber;
}
