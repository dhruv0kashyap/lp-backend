package com.linkedin.dto.request;

import com.linkedin.enums.PostStatus;
import com.linkedin.enums.PostType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostRequest {

    @NotBlank(message = "Please provide a valid content")
    private String content;

    private PostType postType = PostType.POST;

    private PostStatus status = PostStatus.PUBLISHED;

    private LocalDateTime scheduledAt;

    private String imageUrl;

    private List<String> hashtags;
}
