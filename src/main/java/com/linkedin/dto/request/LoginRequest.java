package com.linkedin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequest {

    @NotBlank(message = "Please provide a valid email")
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank(message = "Please provide a valid password")
    private String password;
}
