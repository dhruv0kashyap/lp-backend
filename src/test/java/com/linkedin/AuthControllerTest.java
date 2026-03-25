package com.linkedin;

import com.linkedin.controller.AuthController;
import com.linkedin.dto.request.ForgotPasswordRequest;
import com.linkedin.dto.request.LoginRequest;
import com.linkedin.dto.request.RegisterRequest;
import com.linkedin.dto.request.ResetPasswordRequest;
import com.linkedin.dto.response.ApiResponse;
import com.linkedin.dto.response.AuthResponse;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

@Mock
private AuthService authService;

@InjectMocks
private AuthController authController;

@Test
void register_Success() {
    RegisterRequest request = new RegisterRequest();
    request.setFirstName("John");
    request.setLastName("Doe");
    request.setEmail("john.doe@example.com");
    request.setPassword("Password@123");
    request.setConfirmPassword("Password@123");
    request.setPhoneNumber("9876543210");

    AuthResponse authResponse = AuthResponse.builder()
            .token("testToken")
            .tokenType("Bearer")
            .build();

    when(authService.register(request)).thenReturn(authResponse);

    ResponseEntity<ApiResponse<AuthResponse>> response = authController.register(request);

    assertEquals(201, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Account created successfully! Welcome to LinkedIn.", response.getBody().getMessage());
    assertNotNull(response.getBody().getData());
    assertEquals("testToken", response.getBody().getData().getToken());
}

@Test
void login_Success() {
    LoginRequest request = new LoginRequest();
    request.setEmail("john.doe@example.com");
    request.setPassword("Password@123");

    AuthResponse authResponse = AuthResponse.builder()
            .token("testToken")
            .tokenType("Bearer")
            .build();

    when(authService.login(request)).thenReturn(authResponse);

    ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Login successful", response.getBody().getMessage());
    assertEquals("testToken", response.getBody().getData().getToken());
}

@Test
void login_Unauthorized_Returns401() {
    LoginRequest request = new LoginRequest();
    request.setEmail("john.doe@example.com");
    request.setPassword("wrongPassword");

    when(authService.login(request))
            .thenThrow(new UnauthorizedException("Invalid email or password"));

    ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

    assertEquals(401, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Invalid email or password", response.getBody().getMessage());
}

@Test
void login_CircuitOpen_Returns423() {
    LoginRequest request = new LoginRequest();
    request.setEmail("john.doe@example.com");
    request.setPassword("wrongPassword");

    when(authService.login(request))
            .thenThrow(new UnauthorizedException(
                    "CIRCUIT_OPEN: Too many failed login attempts. Please wait 60 seconds before trying again."
            ));

    ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

    assertEquals(423, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertFalse(response.getBody().isSuccess());
    assertEquals("Too many failed login attempts. Please wait 60 seconds before trying again.",
            response.getBody().getMessage());
}

@Test
void verifyPhone_Success() {
    ForgotPasswordRequest request = new ForgotPasswordRequest();
    request.setEmail("john.doe@example.com");
    request.setPhoneNumber("9876543210");

    when(authService.verifyPhoneForReset(request)).thenReturn("resetToken");

    ResponseEntity<ApiResponse<String>> response = authController.verifyPhone(request);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Phone verified. You can now reset your password.", response.getBody().getMessage());
    assertEquals("resetToken", response.getBody().getData());
}

@Test
void resetPassword_Success() {
    ResetPasswordRequest request = new ResetPasswordRequest();
    request.setEmail("john.doe@example.com");
    request.setNewPassword("NewPassword@123");
    request.setConfirmPassword("NewPassword@123");

    when(authService.resetPassword(request)).thenReturn("Password reset successfully");

    ResponseEntity<ApiResponse<String>> response = authController.resetPassword(request);

    assertEquals(200, response.getStatusCode().value());
    assertNotNull(response.getBody());
    assertTrue(response.getBody().isSuccess());
    assertEquals("Password reset successfully", response.getBody().getMessage());
    assertEquals("Password reset successfully", response.getBody().getData());
}

}