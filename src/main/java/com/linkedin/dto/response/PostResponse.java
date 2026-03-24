package com.linkedin.dto.response;

import com.linkedin.enums.PostStatus;
import com.linkedin.enums.PostType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostResponse {
    private Long id;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userProfilePhotoUrl;
    private String userHeadline;
    private String content;
    private PostType postType;
    private PostStatus status;
    private LocalDateTime scheduledAt;
    private String imageUrl;
    private List<String> hashtags;
    private long likeCount;
    private long commentCount;
    private boolean likedByCurrentUser;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
