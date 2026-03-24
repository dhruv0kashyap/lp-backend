package com.linkedin.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileUpdateRequest {

    @Size(max = 220, message = "Headline must not exceed 220 characters")
    private String headline;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    private String summary;

    private String profilePhotoUrl;
}
