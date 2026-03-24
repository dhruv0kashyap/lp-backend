package com.linkedin.serviceImpl;

import com.linkedin.dto.response.NotificationResponse;
import com.linkedin.entity.Notification;
import com.linkedin.entity.User;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.NotificationRepository;
import com.linkedin.repository.UserRepository;
import com.linkedin.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public Page<NotificationResponse> getNotifications(String email, Pageable pageable) {
        User user = getUserByEmail(email);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    @Override
    public long getUnreadCount(String email) {
        User user = getUserByEmail(email);
        return notificationRepository.countByUserIdAndIsReadFalse(user.getId());
    }

    @Override
    @Transactional
    public void markAsRead(String email, Long notificationId) {
        User user = getUserByEmail(email);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to update this notification");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String email) {
        User user = getUserByEmail(email);
        notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), Pageable.unpaged())
                .forEach(n -> { n.setIsRead(true); notificationRepository.save(n); });
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .userId(n.getUser().getId())
                .type(n.getType())
                .message(n.getMessage())
                .referenceId(n.getReferenceId())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
