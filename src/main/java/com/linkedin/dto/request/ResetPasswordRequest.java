package com.linkedin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ResetPasswordRequest {

    @NotBlank(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Please provide a valid newPassword")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
            message = "Password must contain at least one uppercase, lowercase, digit and special character")
    private String newPassword;

    @NotBlank(message = "Please provide a valid confirmPassword")
    private String confirmPassword;
}
