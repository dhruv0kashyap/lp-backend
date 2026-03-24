package com.linkedin.serviceImpl;

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
import com.linkedin.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectionServiceImpl implements ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public ConnectionResponse sendConnectionRequest(String email, Long receiverId) {
        User sender = getUserByEmail(email);
        if (sender.getId().equals(receiverId)) {
            throw new BadRequestException("You cannot connect with yourself");
        }
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + receiverId));

        // Bug fix: check BOTH directions (sender→receiver AND receiver→sender)
        boolean alreadyConnected = connectionRepository.existsBetweenUsersWithStatus(
                sender.getId(), receiverId, ConnectionStatus.ACCEPTED);
        boolean pendingExists = connectionRepository.existsBetweenUsersWithStatus(
                sender.getId(), receiverId, ConnectionStatus.PENDING);

        if (alreadyConnected) {
            throw new BadRequestException("You are already connected with this user");
        }
        if (pendingExists) {
            throw new BadRequestException("A connection request is already pending between you and this user");
        }

        Connection connection = Connection.builder()
                .sender(sender)
                .receiver(receiver)
                .status(ConnectionStatus.PENDING)
                .build();
        connection = connectionRepository.save(connection);

        notificationRepository.save(Notification.builder()
                .user(receiver)
                .type(NotificationType.CONNECTION_REQUEST)
                .message(sender.getFirstName() + " " + sender.getLastName() + " sent you a connection request")
                .referenceId(connection.getId())
                .isRead(false)
                .build());

        log.info("Connection request sent from {} to {}", sender.getEmail(), receiver.getEmail());
        return mapToConnectionResponse(connection);
    }

    @Override
    @Transactional
    public ConnectionResponse acceptConnection(String email, Long connectionId) {
        User user = getUserByEmail(email);
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        if (!connection.getReceiver().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to accept this request");
        }
        if (!connection.getStatus().equals(ConnectionStatus.PENDING)) {
            throw new BadRequestException("Connection request is not pending");
        }

        connection.setStatus(ConnectionStatus.ACCEPTED);
        connection = connectionRepository.save(connection);

        // Notify sender that connection was accepted
        notificationRepository.save(Notification.builder()
                .user(connection.getSender())
                .type(NotificationType.CONNECTION_ACCEPTED)
                .message(user.getFirstName() + " " + user.getLastName() + " accepted your connection request")
                .referenceId(connection.getId())
                .isRead(false)
                .build());

        log.info("Connection accepted between {} and {}", connection.getSender().getEmail(), user.getEmail());
        return mapToConnectionResponse(connection);
    }

    @Override
    @Transactional
    public ConnectionResponse rejectConnection(String email, Long connectionId) {
        User user = getUserByEmail(email);
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        if (!connection.getReceiver().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to reject this request");
        }
        connection.setStatus(ConnectionStatus.REJECTED);
        return mapToConnectionResponse(connectionRepository.save(connection));
    }

    @Override
    @Transactional
    public void removeConnection(String email, Long connectionId) {
        User user = getUserByEmail(email);
        Connection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Connection not found"));

        if (!connection.getSender().getId().equals(user.getId())
                && !connection.getReceiver().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to remove this connection");
        }
        connectionRepository.delete(connection);
        log.info("Connection {} removed by {}", connectionId, email);
    }

    @Override
    public List<ConnectionResponse> getPendingRequests(String email) {
        User user = getUserByEmail(email);
        return connectionRepository.findByUserIdAndStatus(user.getId(), ConnectionStatus.PENDING)
                .stream()
                .filter(c -> c.getReceiver().getId().equals(user.getId()))
                .map(this::mapToConnectionResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getConnections(String email) {
        User user = getUserByEmail(email);
        List<Long> connectionIds = connectionRepository.findConnectionUserIds(user.getId());
        return userRepository.findAllById(connectionIds).stream()
                .map(u -> UserResponse.builder()
                        .id(u.getId())
                        .firstName(u.getFirstName())
                        .lastName(u.getLastName())
                        .email(u.getEmail())
                        .profilePhotoUrl(u.getProfilePhotoUrl())
                        .headline(u.getHeadline())
                        .location(u.getLocation())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getConnectionStatus(String email, Long targetUserId) {
        User currentUser = getUserByEmail(email);
        Map<String, Object> result = new HashMap<>();

        Optional<Connection> conn = connectionRepository.findConnectionBetween(
                currentUser.getId(), targetUserId);

        if (conn.isEmpty()) {
            result.put("status", "NONE");
            result.put("connectionId", null);
        } else {
            Connection c = conn.get();
            if (c.getStatus() == ConnectionStatus.ACCEPTED) {
                result.put("status", "CONNECTED");
                result.put("connectionId", c.getId());
            } else if (c.getStatus() == ConnectionStatus.PENDING) {
                // SENT = current user sent the request, RECEIVED = current user got it
                if (c.getSender().getId().equals(currentUser.getId())) {
                    result.put("status", "SENT");
                } else {
                    result.put("status", "RECEIVED");
                }
                result.put("connectionId", c.getId());
            } else {
                result.put("status", "NONE");
                result.put("connectionId", null);
            }
        }
        return result;
    }

    private ConnectionResponse mapToConnectionResponse(Connection c) {
        return ConnectionResponse.builder()
                .id(c.getId())
                .senderId(c.getSender().getId())
                .senderFirstName(c.getSender().getFirstName())
                .senderLastName(c.getSender().getLastName())
                .senderProfilePhotoUrl(c.getSender().getProfilePhotoUrl())
                .senderHeadline(c.getSender().getHeadline())
                .receiverId(c.getReceiver().getId())
                .receiverFirstName(c.getReceiver().getFirstName())
                .receiverLastName(c.getReceiver().getLastName())
                .receiverProfilePhotoUrl(c.getReceiver().getProfilePhotoUrl())
                .receiverHeadline(c.getReceiver().getHeadline())
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
