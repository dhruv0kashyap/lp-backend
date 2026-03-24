package com.linkedin.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RegisterRequest {

    @NotBlank(message = "Please provide a valid firstName")
    @Pattern(regexp = "^[A-Z][a-zA-Z]*$", message = "First name must start with a capital letter and contain only English letters")
    @Size(min = 1, message = "Please provide a valid firstName")
    private String firstName;

    @NotBlank(message = "Please provide a valid lastName")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Last name must contain only English letters")
    @Size(min = 1, message = "Please provide a valid lastName")
    private String lastName;

    @NotBlank(message = "Please provide a valid email")
    @Email(message = "Please provide a valid email")
    @Pattern(regexp = "^[\\w._%+-]+@[\\w.-]+\\.(com|org|in)$", message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Please provide a valid password")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
            message = "Password must contain at least one uppercase, lowercase, digit and special character")
    private String password;

    @NotBlank(message = "Please provide a valid confirmPassword")
    private String confirmPassword;

    @Pattern(regexp = "^[0-9]{10}$", message = "Please provide a valid phoneNumber")
    private String phoneNumber;

    // Optional profile photo URL uploaded during registration
    private String profilePhotoUrl;
}
