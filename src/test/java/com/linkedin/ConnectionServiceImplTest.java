package com.linkedin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.linkedin.dto.response.ConnectionResponse;
import com.linkedin.dto.response.UserResponse;
import com.linkedin.entity.Connection;
import com.linkedin.entity.Notification;
import com.linkedin.entity.User;
import com.linkedin.enums.ConnectionStatus;
import com.linkedin.enums.NotificationType;
import com.linkedin.exception.BadRequestException;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.ConnectionRepository;
import com.linkedin.repository.NotificationRepository;
import com.linkedin.repository.UserRepository;
import com.linkedin.serviceImpl.ConnectionServiceImpl;

@ExtendWith(MockitoExtension.class)
class ConnectionServiceImplTest {

@Mock
private ConnectionRepository connectionRepository;

@Mock
private UserRepository userRepository;

@Mock
private NotificationRepository notificationRepository;

@InjectMocks
private ConnectionServiceImpl connectionService;

private User buildUser(Long id, String email, String firstName, String lastName) {
    return User.builder()
            .id(id)
            .email(email)
            .firstName(firstName)
            .lastName(lastName)
            .profilePhotoUrl("photo-" + id + ".png")
            .headline("headline-" + id)
            .location("Bangalore")
            .build();
}

private Connection buildConnection(Long id, User sender, User receiver, ConnectionStatus status) {
    return Connection.builder()
            .id(id)
            .sender(sender)
            .receiver(receiver)
            .status(status)
            .createdAt(LocalDateTime.now())
            .build();
}

@Test
void sendConnectionRequest_Success() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");

    Connection savedConnection = buildConnection(10L, sender, receiver, ConnectionStatus.PENDING);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sender));
    when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
    when(connectionRepository.existsBetweenUsersWithStatus(1L, 2L, ConnectionStatus.ACCEPTED)).thenReturn(false);
    when(connectionRepository.existsBetweenUsersWithStatus(1L, 2L, ConnectionStatus.PENDING)).thenReturn(false);
    when(connectionRepository.save(any(Connection.class))).thenReturn(savedConnection);

    ConnectionResponse response = connectionService.sendConnectionRequest("john@example.com", 2L);

    assertNotNull(response);
    assertEquals(10L, response.getId());
    assertEquals(1L, response.getSenderId());
    assertEquals(2L, response.getReceiverId());
    assertEquals(ConnectionStatus.PENDING, response.getStatus());

    verify(connectionRepository).save(any(Connection.class));
    verify(notificationRepository).save(any(Notification.class));
}

@Test
void sendConnectionRequest_SelfConnection_ThrowsBadRequest() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sender));

    assertThrows(BadRequestException.class,
            () -> connectionService.sendConnectionRequest("john@example.com", 1L));

    verify(connectionRepository, never()).save(any());
    verify(notificationRepository, never()).save(any());
}

@Test
void sendConnectionRequest_ReceiverNotFound_ThrowsResourceNotFound() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sender));
    when(userRepository.findById(2L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> connectionService.sendConnectionRequest("john@example.com", 2L));
}

@Test
void sendConnectionRequest_AlreadyConnected_ThrowsBadRequest() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sender));
    when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
    when(connectionRepository.existsBetweenUsersWithStatus(1L, 2L, ConnectionStatus.ACCEPTED)).thenReturn(true);

    assertThrows(BadRequestException.class,
            () -> connectionService.sendConnectionRequest("john@example.com", 2L));

    verify(connectionRepository, never()).save(any());
    verify(notificationRepository, never()).save(any());
}

@Test
void sendConnectionRequest_PendingAlreadyExists_ThrowsBadRequest() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sender));
    when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
    when(connectionRepository.existsBetweenUsersWithStatus(1L, 2L, ConnectionStatus.ACCEPTED)).thenReturn(false);
    when(connectionRepository.existsBetweenUsersWithStatus(1L, 2L, ConnectionStatus.PENDING)).thenReturn(true);

    assertThrows(BadRequestException.class,
            () -> connectionService.sendConnectionRequest("john@example.com", 2L));

    verify(connectionRepository, never()).save(any());
    verify(notificationRepository, never()).save(any());
}

@Test
void acceptConnection_Success() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(5L, sender, receiver, ConnectionStatus.PENDING);
    Connection acceptedConnection = buildConnection(5L, sender, receiver, ConnectionStatus.ACCEPTED);

    when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(receiver));
    when(connectionRepository.findById(5L)).thenReturn(Optional.of(connection));
    when(connectionRepository.save(any(Connection.class))).thenReturn(acceptedConnection);

    ConnectionResponse response = connectionService.acceptConnection("jane@example.com", 5L);

    assertNotNull(response);
    assertEquals(ConnectionStatus.ACCEPTED, response.getStatus());

    ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
    verify(notificationRepository).save(notificationCaptor.capture());
    assertEquals(NotificationType.CONNECTION_ACCEPTED, notificationCaptor.getValue().getType());
    assertEquals(sender, notificationCaptor.getValue().getUser());
}

@Test
void acceptConnection_NotReceiver_ThrowsUnauthorized() {
    User user = buildUser(3L, "other@example.com", "Other", "User");
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(5L, sender, receiver, ConnectionStatus.PENDING);

    when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(user));
    when(connectionRepository.findById(5L)).thenReturn(Optional.of(connection));

    assertThrows(UnauthorizedException.class,
            () -> connectionService.acceptConnection("other@example.com", 5L));
}

@Test
void acceptConnection_NotPending_ThrowsBadRequest() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(5L, sender, receiver, ConnectionStatus.ACCEPTED);

    when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(receiver));
    when(connectionRepository.findById(5L)).thenReturn(Optional.of(connection));

    assertThrows(BadRequestException.class,
            () -> connectionService.acceptConnection("jane@example.com", 5L));
}

@Test
void rejectConnection_Success() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(5L, sender, receiver, ConnectionStatus.PENDING);
    Connection rejectedConnection = buildConnection(5L, sender, receiver, ConnectionStatus.REJECTED);

    when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(receiver));
    when(connectionRepository.findById(5L)).thenReturn(Optional.of(connection));
    when(connectionRepository.save(any(Connection.class))).thenReturn(rejectedConnection);

    ConnectionResponse response = connectionService.rejectConnection("jane@example.com", 5L);

    assertNotNull(response);
    assertEquals(ConnectionStatus.REJECTED, response.getStatus());
}

@Test
void rejectConnection_NotReceiver_ThrowsUnauthorized() {
    User other = buildUser(3L, "other@example.com", "Other", "User");
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(5L, sender, receiver, ConnectionStatus.PENDING);

    when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(other));
    when(connectionRepository.findById(5L)).thenReturn(Optional.of(connection));

    assertThrows(UnauthorizedException.class,
            () -> connectionService.rejectConnection("other@example.com", 5L));
}

@Test
void removeConnection_SenderCanRemove_Success() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(5L, sender, receiver, ConnectionStatus.ACCEPTED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(sender));
    when(connectionRepository.findById(5L)).thenReturn(Optional.of(connection));

    connectionService.removeConnection("john@example.com", 5L);

    verify(connectionRepository).delete(connection);
}

@Test
void removeConnection_ReceiverCanRemove_Success() {
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(5L, sender, receiver, ConnectionStatus.ACCEPTED);

    when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(receiver));
    when(connectionRepository.findById(5L)).thenReturn(Optional.of(connection));

    connectionService.removeConnection("jane@example.com", 5L);

    verify(connectionRepository).delete(connection);
}

@Test
void removeConnection_Unauthorized_ThrowsException() {
    User other = buildUser(3L, "other@example.com", "Other", "User");
    User sender = buildUser(1L, "john@example.com", "John", "Doe");
    User receiver = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(5L, sender, receiver, ConnectionStatus.ACCEPTED);

    when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(other));
    when(connectionRepository.findById(5L)).thenReturn(Optional.of(connection));

    assertThrows(UnauthorizedException.class,
            () -> connectionService.removeConnection("other@example.com", 5L));
}

@Test
void getPendingRequests_ReturnsOnlyRequestsWhereCurrentUserIsReceiver() {
    User current = buildUser(2L, "jane@example.com", "Jane", "Doe");
    User sender1 = buildUser(1L, "john@example.com", "John", "Doe");
    User sender2 = buildUser(3L, "mike@example.com", "Mike", "Ross");

    Connection incoming = buildConnection(11L, sender1, current, ConnectionStatus.PENDING);
    Connection outgoing = buildConnection(12L, current, sender2, ConnectionStatus.PENDING);

    when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(current));
    when(connectionRepository.findByUserIdAndStatus(2L, ConnectionStatus.PENDING))
            .thenReturn(List.of(incoming, outgoing));

    List<ConnectionResponse> responses = connectionService.getPendingRequests("jane@example.com");

    assertEquals(1, responses.size());
    assertEquals(11L, responses.get(0).getId());
    assertEquals(1L, responses.get(0).getSenderId());
    assertEquals(2L, responses.get(0).getReceiverId());
}

@Test
void getConnections_Success() {
    User current = buildUser(1L, "john@example.com", "John", "Doe");
    User conn1 = buildUser(2L, "jane@example.com", "Jane", "Doe");
    User conn2 = buildUser(3L, "mike@example.com", "Mike", "Ross");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(current));
    when(connectionRepository.findConnectionUserIds(1L)).thenReturn(List.of(2L, 3L));
    when(userRepository.findAllById(List.of(2L, 3L))).thenReturn(List.of(conn1, conn2));

    List<UserResponse> responses = connectionService.getConnections("john@example.com");

    assertEquals(2, responses.size());
    assertEquals("jane@example.com", responses.get(0).getEmail());
    assertEquals("mike@example.com", responses.get(1).getEmail());
}

@Test
void getConnectionStatus_NoConnection_ReturnsNone() {
    User current = buildUser(1L, "john@example.com", "John", "Doe");

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(current));
    when(connectionRepository.findConnectionBetween(1L, 2L)).thenReturn(Optional.empty());

    Map<String, Object> result = connectionService.getConnectionStatus("john@example.com", 2L);

    assertEquals("NONE", result.get("status"));
    assertNull(result.get("connectionId"));
}

@Test
void getConnectionStatus_Accepted_ReturnsConnected() {
    User current = buildUser(1L, "john@example.com", "John", "Doe");
    User target = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(99L, current, target, ConnectionStatus.ACCEPTED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(current));
    when(connectionRepository.findConnectionBetween(1L, 2L)).thenReturn(Optional.of(connection));

    Map<String, Object> result = connectionService.getConnectionStatus("john@example.com", 2L);

    assertEquals("CONNECTED", result.get("status"));
    assertEquals(99L, result.get("connectionId"));
}

@Test
void getConnectionStatus_PendingSent_ReturnsSent() {
    User current = buildUser(1L, "john@example.com", "John", "Doe");
    User target = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(99L, current, target, ConnectionStatus.PENDING);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(current));
    when(connectionRepository.findConnectionBetween(1L, 2L)).thenReturn(Optional.of(connection));

    Map<String, Object> result = connectionService.getConnectionStatus("john@example.com", 2L);

    assertEquals("SENT", result.get("status"));
    assertEquals(99L, result.get("connectionId"));
}

@Test
void getConnectionStatus_PendingReceived_ReturnsReceived() {
    User current = buildUser(1L, "john@example.com", "John", "Doe");
    User target = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(99L, target, current, ConnectionStatus.PENDING);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(current));
    when(connectionRepository.findConnectionBetween(1L, 2L)).thenReturn(Optional.of(connection));

    Map<String, Object> result = connectionService.getConnectionStatus("john@example.com", 2L);

    assertEquals("RECEIVED", result.get("status"));
    assertEquals(99L, result.get("connectionId"));
}

@Test
void getConnectionStatus_Rejected_ReturnsNone() {
    User current = buildUser(1L, "john@example.com", "John", "Doe");
    User target = buildUser(2L, "jane@example.com", "Jane", "Doe");
    Connection connection = buildConnection(99L, current, target, ConnectionStatus.REJECTED);

    when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(current));
    when(connectionRepository.findConnectionBetween(1L, 2L)).thenReturn(Optional.of(connection));

    Map<String, Object> result = connectionService.getConnectionStatus("john@example.com", 2L);

    assertEquals("NONE", result.get("status"));
    assertNull(result.get("connectionId"));
}

@Test
void getUserByEmail_UserNotFound_ThrowsResourceNotFoundIndirectly() {
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class,
            () -> connectionService.getPendingRequests("missing@example.com"));
}

}
