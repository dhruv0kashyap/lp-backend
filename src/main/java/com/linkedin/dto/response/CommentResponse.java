package com.linkedin.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommentResponse {
    private Long id;
    private Long postId;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userProfilePhotoUrl;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
