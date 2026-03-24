package com.linkedin.dto.response;

import com.linkedin.enums.ConnectionStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConnectionResponse {
    private Long id;
    private Long senderId;
    private String senderFirstName;
    private String senderLastName;
    private String senderProfilePhotoUrl;
    private String senderHeadline;
    private Long receiverId;
    private String receiverFirstName;
    private String receiverLastName;
    private String receiverProfilePhotoUrl;
    private String receiverHeadline;
    private ConnectionStatus status;
    private LocalDateTime createdAt;
}
