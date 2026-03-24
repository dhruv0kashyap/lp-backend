package com.linkedin.controller;

import com.linkedin.dto.response.*;
import com.linkedin.service.ConnectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/connections")
@RequiredArgsConstructor
@Tag(name = "Connections", description = "Networking and connection APIs")
public class ConnectionController {

    private final ConnectionService connectionService;

    @PostMapping("/request/{receiverId}")
    @Operation(summary = "Send a connection request")
    public ResponseEntity<ApiResponse<ConnectionResponse>> sendRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long receiverId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Connection request sent",
                        connectionService.sendConnectionRequest(userDetails.getUsername(), receiverId)));
    }

    @PutMapping("/{connectionId}/accept")
    @Operation(summary = "Accept a connection request")
    public ResponseEntity<ApiResponse<ConnectionResponse>> acceptConnection(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long connectionId) {
        return ResponseEntity.ok(ApiResponse.success("Connection accepted",
                connectionService.acceptConnection(userDetails.getUsername(), connectionId)));
    }

    @PutMapping("/{connectionId}/reject")
    @Operation(summary = "Reject a connection request")
    public ResponseEntity<ApiResponse<ConnectionResponse>> rejectConnection(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long connectionId) {
        return ResponseEntity.ok(ApiResponse.success("Connection rejected",
                connectionService.rejectConnection(userDetails.getUsername(), connectionId)));
    }

    @DeleteMapping("/{connectionId}")
    @Operation(summary = "Remove a connection")
    public ResponseEntity<ApiResponse<Void>> removeConnection(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long connectionId) {
        connectionService.removeConnection(userDetails.getUsername(), connectionId);
        return ResponseEntity.ok(ApiResponse.success("Connection removed"));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get pending connection requests")
    public ResponseEntity<ApiResponse<List<ConnectionResponse>>> getPendingRequests(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Pending requests fetched",
                connectionService.getPendingRequests(userDetails.getUsername())));
    }

    @GetMapping
    @Operation(summary = "Get all connections")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getConnections(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Connections fetched",
                connectionService.getConnections(userDetails.getUsername())));
    }

    // Bug fix 1: New endpoint to check real connection status between two users
    @GetMapping("/status/{targetUserId}")
    @Operation(summary = "Get connection status with a specific user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConnectionStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long targetUserId) {
        return ResponseEntity.ok(ApiResponse.success("Status fetched",
                connectionService.getConnectionStatus(userDetails.getUsername(), targetUserId)));
    }
}
