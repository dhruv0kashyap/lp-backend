package com.linkedin.controller;

import com.linkedin.dto.request.ForgotPasswordRequest;
import com.linkedin.dto.request.LoginRequest;
import com.linkedin.dto.request.RegisterRequest;
import com.linkedin.dto.request.ResetPasswordRequest;
import com.linkedin.dto.response.ApiResponse;
import com.linkedin.dto.response.AuthResponse;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User registration and login APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user (US01)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully! Welcome to LinkedIn.", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login user with circuit breaker protection (US02)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (UnauthorizedException ex) {
            // Distinguish circuit-open from plain bad-credentials
            if (ex.getMessage() != null && ex.getMessage().startsWith("CIRCUIT_OPEN:")) {
                return ResponseEntity.status(423) // 423 Locked
                        .body(ApiResponse.<AuthResponse>builder()
                                .success(false)
                                .message(ex.getMessage().replace("CIRCUIT_OPEN: ", ""))
                                .build());
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ex.getMessage()));
        }
    }

    @PostMapping("/forgot-password/verify")
    @Operation(summary = "Verify phone number for password reset (US02)")
    public ResponseEntity<ApiResponse<String>> verifyPhone(@Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.verifyPhoneForReset(request);
        return ResponseEntity.ok(ApiResponse.success("Phone verified. You can now reset your password.", token));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password after phone verification (US02)")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message, message));
    }
}
