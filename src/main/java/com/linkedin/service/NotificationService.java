package com.linkedin.service;

import com.linkedin.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Page<NotificationResponse> getNotifications(String email, Pageable pageable);
    long getUnreadCount(String email);
    void markAsRead(String email, Long notificationId);
    void markAllAsRead(String email);
}
