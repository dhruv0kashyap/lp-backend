package com.linkedin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.linkedin.controller.ConnectionController;
import com.linkedin.dto.response.ApiResponse;
import com.linkedin.dto.response.ConnectionResponse;
import com.linkedin.dto.response.UserResponse;
import com.linkedin.service.ConnectionService;

@ExtendWith(MockitoExtension.class)
class ConnectionControllerTest {

@Mock
private ConnectionService connectionService;

@InjectMocks
private ConnectionController connectionController;

private UserDetails getUserDetails() {
    return User.withUsername("john.doe@example.com")
            .password("password")
            .authorities(Collections.emptyList())
            .build();
}

@Test
void sendRequest_Success() {
    UserDetails userDetails = getUserDetails();
    ConnectionResponse connectionResponse = new ConnectionResponse();

    when(connectionService.sendConnectionRequest("john.doe@example.com", 2L))
            .thenReturn(connectionResponse);

    ResponseEntity<ApiResponse<ConnectionResponse>> response =
            connectionController.sendRequest(userDetails, 2L);

    assertEquals(201, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Connection request sent", response.getBody().getMessage());
    assertEquals(connectionResponse, response.getBody().getData());
}

@Test
void acceptConnection_Success() {
    UserDetails userDetails = getUserDetails();
    ConnectionResponse connectionResponse = new ConnectionResponse();

    when(connectionService.acceptConnection("john.doe@example.com", 1L))
            .thenReturn(connectionResponse);

    ResponseEntity<ApiResponse<ConnectionResponse>> response =
            connectionController.acceptConnection(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Connection accepted", response.getBody().getMessage());
    assertEquals(connectionResponse, response.getBody().getData());
}

@Test
void rejectConnection_Success() {
    UserDetails userDetails = getUserDetails();
    ConnectionResponse connectionResponse = new ConnectionResponse();

    when(connectionService.rejectConnection("john.doe@example.com", 1L))
            .thenReturn(connectionResponse);

    ResponseEntity<ApiResponse<ConnectionResponse>> response =
            connectionController.rejectConnection(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Connection rejected", response.getBody().getMessage());
    assertEquals(connectionResponse, response.getBody().getData());
}

@Test
void removeConnection_Success() {
    UserDetails userDetails = getUserDetails();

    doNothing().when(connectionService).removeConnection("john.doe@example.com", 1L);

    ResponseEntity<ApiResponse<Void>> response =
            connectionController.removeConnection(userDetails, 1L);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Connection removed", response.getBody().getMessage());
    verify(connectionService).removeConnection("john.doe@example.com", 1L);
}

@Test
void getPendingRequests_Success() {
    UserDetails userDetails = getUserDetails();
    List<ConnectionResponse> pendingRequests = List.of(new ConnectionResponse());

    when(connectionService.getPendingRequests("john.doe@example.com"))
            .thenReturn(pendingRequests);

    ResponseEntity<ApiResponse<List<ConnectionResponse>>> response =
            connectionController.getPendingRequests(userDetails);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Pending requests fetched", response.getBody().getMessage());
    assertEquals(pendingRequests, response.getBody().getData());
}

@Test
void getConnections_Success() {
    UserDetails userDetails = getUserDetails();
    List<UserResponse> connections = List.of(new UserResponse());

    when(connectionService.getConnections("john.doe@example.com"))
            .thenReturn(connections);

    ResponseEntity<ApiResponse<List<UserResponse>>> response =
            connectionController.getConnections(userDetails);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Connections fetched", response.getBody().getMessage());
    assertEquals(connections, response.getBody().getData());
}

@Test
void getConnectionStatus_Success() {
    UserDetails userDetails = getUserDetails();
    Map<String, Object> statusMap = new HashMap<>();
    statusMap.put("connected", true);

    when(connectionService.getConnectionStatus("john.doe@example.com", 5L))
            .thenReturn(statusMap);

    ResponseEntity<ApiResponse<Map<String, Object>>> response =
            connectionController.getConnectionStatus(userDetails, 5L);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Status fetched", response.getBody().getMessage());
    assertEquals(statusMap, response.getBody().getData());
}

}

