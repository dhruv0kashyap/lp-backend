package com.linkedin.service;

import com.linkedin.dto.response.ConnectionResponse;
import com.linkedin.dto.response.UserResponse;
import java.util.List;
import java.util.Map;

public interface ConnectionService {
    ConnectionResponse sendConnectionRequest(String email, Long receiverId);
    ConnectionResponse acceptConnection(String email, Long connectionId);
    ConnectionResponse rejectConnection(String email, Long connectionId);
    void removeConnection(String email, Long connectionId);
    List<ConnectionResponse> getPendingRequests(String email);
    List<UserResponse> getConnections(String email);
    // Returns connection status between current user and target: NONE, PENDING, ACCEPTED, SENT
    Map<String, Object> getConnectionStatus(String email, Long targetUserId);
}
