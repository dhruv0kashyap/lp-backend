package com.linkedin;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.linkedin.dto.response.NotificationResponse;
import com.linkedin.entity.Notification;
import com.linkedin.entity.User;
import com.linkedin.enums.NotificationType;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.NotificationRepository;
import com.linkedin.repository.UserRepository;
import com.linkedin.serviceImpl.NotificationServiceImpl;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

@Mock
private NotificationRepository notificationRepository;

@Mock
private UserRepository userRepository;

@InjectMocks
private NotificationServiceImpl notificationService;

private User buildUser(Long id, String email, String firstName, String lastName) {
    return User.builder()
            .id(id)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .build();
}

private Notification buildNotification(Long id, User user, boolean isRead) {
    return Notification.builder()
            .id(id)
            .user(user)
            .type(NotificationType.CONNECTION_REQUEST)
            .message("You have a new connection request")
            .referenceId(100L)
            .isRead(isRead)
            .createdAt(LocalDateTime.now())
            .build();
}

@Test
void getNotifications_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Notification notification = buildNotification(10L, user, false);

    Pageable pageable = PageRequest.of(0, 10);
    Page<Notification> page = new PageImpl<>(List.of(notification), pageable, 1);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable)).thenReturn(page);

    Page<NotificationResponse> response =
            notificationService.getNotifications("john@example.com", pageable);

    assertNotNull(response);
    assertEquals(1, response.getContent().size());
    assertEquals(10L, response.getContent().get(0).getId());
    assertEquals("You have a new connection request", response.getContent().get(0).getMessage());
}

@Test
void getNotifications_UserNotFound_ThrowsException() {
    Pageable pageable = PageRequest.of(0, 10);
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> notificationService.getNotifications("missing@example.com", pageable));
}

@Test
void getUnreadCount_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

    long count = notificationService.getUnreadCount("john@example.com");

    assertEquals(5L, count);
}

@Test
void getUnreadCount_UserNotFound_ThrowsException() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> notificationService.getUnreadCount("missing@example.com"));
}

@Test
void markAsRead_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Notification notification = buildNotification(10L, user, false);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

    notificationService.markAsRead("john@example.com", 10L);

    assertTrue(notification.getIsRead());
    verify(notificationRepository).save(notification);
}

@Test
void markAsRead_NotificationNotFound_ThrowsException() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(notificationRepository.findById(10L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> notificationService.markAsRead("john@example.com", 10L));
}

@Test
void markAsRead_Unauthorized_ThrowsException() {
    User currentUser = buildUser(1L, "john@example.com", "John", "Doe");
    User owner = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Notification notification = buildNotification(10L, owner, false);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    when(notificationRepository.findById(10L)).thenReturn(Optional.of(notification));

    assertThrows(UnauthorizedException.class,
            () -> notificationService.markAsRead("john@example.com", 10L));
}

@Test
void markAllAsRead_Success() {
    User user = buildUser(1L, "john@example.com", "John", "Doe");
    Notification n1 = buildNotification(10L, user, false);
    Notification n2 = buildNotification(11L, user, false);

    Page<Notification> page = new PageImpl<>(List.of(n1, n2));

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
    when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L, Pageable.unpaged())).thenReturn(page);

    notificationService.markAllAsRead("john@example.com");

    assertTrue(n1.getIsRead());
    assertTrue(n2.getIsRead());
    verify(notificationRepository, times(2)).save(any(Notification.class));
}

@Test
void markAllAsRead_UserNotFound_ThrowsException() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> notificationService.markAllAsRead("missing@example.com"));
}

}


