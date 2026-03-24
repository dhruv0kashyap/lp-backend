package com.linkedin.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentRequest {

    @NotBlank(message = "Please provide a valid content")
    private String content;
}
