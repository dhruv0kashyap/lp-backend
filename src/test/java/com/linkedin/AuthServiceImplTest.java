package com.linkedin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.linkedin.dto.request.ForgotPasswordRequest;
import com.linkedin.dto.request.LoginRequest;
import com.linkedin.dto.request.RegisterRequest;
import com.linkedin.dto.request.ResetPasswordRequest;
import com.linkedin.dto.response.AuthResponse;
import com.linkedin.entity.User;
import com.linkedin.exception.BadRequestException;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.UserRepository;
import com.linkedin.security.JwtUtil;
import com.linkedin.serviceImpl.AuthServiceImpl;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

@Mock
private UserRepository userRepository;

@Mock
private PasswordEncoder passwordEncoder;

@Mock
private JwtUtil jwtUtil;

@Mock
private AuthenticationManager authenticationManager;

@Mock
private ModelMapper modelMapper;

@InjectMocks
private AuthServiceImpl authService;

private RegisterRequest registerRequest;
private LoginRequest loginRequest;
private ForgotPasswordRequest forgotPasswordRequest;
private ResetPasswordRequest resetPasswordRequest;
private User user;

@BeforeEach
void setUp() {
    registerRequest = RegisterRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .password("Password@123")
            .confirmPassword("Password@123")
            .phoneNumber("9876543210")
            .profilePhotoUrl("profile.jpg")
            .build();

    loginRequest = LoginRequest.builder()
            .email("john.doe@example.com")
            .password("Password@123")
            .build();

    forgotPasswordRequest = ForgotPasswordRequest.builder()
            .email("john.doe@example.com")
            .phoneNumber("9876543210")
            .build();

    resetPasswordRequest = ResetPasswordRequest.builder()
            .email("john.doe@example.com")
            .newPassword("NewPassword@123")
            .confirmPassword("NewPassword@123")
            .build();

    user = User.builder()
            .id(1L)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .password("encodedPassword")
            .phoneNumber("9876543210")
            .profilePhotoUrl("profile.jpg")
            .isActive(true)
            .build();
}

@Test
void register_Success() {
    when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);
    when(jwtUtil.generateToken(user.getEmail())).thenReturn("testToken");

    AuthResponse response = authService.register(registerRequest);

    assertNotNull(response);
    assertEquals("testToken", response.getToken());
    assertEquals("Bearer", response.getTokenType());
    verify(userRepository).save(any(User.class));
    verify(passwordEncoder).encode(registerRequest.getPassword());
    verify(jwtUtil).generateToken(user.getEmail());
}

@Test
void register_PasswordMismatch_ThrowsBadRequestException() {
    registerRequest.setConfirmPassword("DifferentPassword@123");

    BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> authService.register(registerRequest)
    );

    assertEquals("Passwords do not match", ex.getMessage());
    verify(userRepository, never()).save(any(User.class));
}

@Test
void register_EmailAlreadyExists_ThrowsBadRequestException() {
    when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

    BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> authService.register(registerRequest)
    );

    assertEquals("This email is already associated with an existing account", ex.getMessage());
    verify(userRepository, never()).save(any(User.class));
}

@Test
void login_Success() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken(user.getEmail())).thenReturn("testToken");

    AuthResponse response = authService.login(loginRequest);

    assertNotNull(response);
    assertEquals("testToken", response.getToken());
    assertEquals("Bearer", response.getTokenType());
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(userRepository).findByEmail(loginRequest.getEmail());
    verify(jwtUtil).generateToken(user.getEmail());
}

@Test
void login_BadCredentials_ThrowsBadCredentialsException() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));

    verify(userRepository, never()).findByEmail(any());
    verify(jwtUtil, never()).generateToken(any());
}

@Test
void login_UserNotFoundAfterAuthentication_ThrowsBadRequestException() {
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
    when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

    BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> authService.login(loginRequest)
    );

    assertEquals("Invalid email or password", ex.getMessage());
    verify(jwtUtil, never()).generateToken(any());
}

@Test
void loginFallback_CircuitOpen_ThrowsUnauthorizedException() {
    CallNotPermittedException exception =
            CallNotPermittedException.createCallNotPermittedException(
                    io.github.resilience4j.circuitbreaker.CircuitBreaker.ofDefaults("loginService")
            );

    UnauthorizedException ex = assertThrows(
            UnauthorizedException.class,
            () -> authService.loginFallback(loginRequest, exception)
    );

    assertTrue(ex.getMessage().contains("CIRCUIT_OPEN"));
    assertTrue(ex.getMessage().contains("Too many failed login attempts"));
}

@Test
void loginFallback_GenericException_ThrowsUnauthorizedException() {
    Exception exception = new Exception("Some login failure");

    UnauthorizedException ex = assertThrows(
            UnauthorizedException.class,
            () -> authService.loginFallback(loginRequest, exception)
    );

    assertEquals("Some login failure", ex.getMessage());
}

@Test
void verifyPhoneForReset_Success() {
    when(userRepository.findByEmail(forgotPasswordRequest.getEmail())).thenReturn(Optional.of(user));
    when(jwtUtil.generateToken(user.getEmail())).thenReturn("resetToken");

    String token = authService.verifyPhoneForReset(forgotPasswordRequest);

    assertEquals("resetToken", token);
    verify(userRepository).findByEmail(forgotPasswordRequest.getEmail());
    verify(jwtUtil).generateToken(user.getEmail());
}

@Test
void verifyPhoneForReset_UserNotFound_ThrowsResourceNotFoundException() {
    when(userRepository.findByEmail(forgotPasswordRequest.getEmail())).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> authService.verifyPhoneForReset(forgotPasswordRequest)
    );

    assertEquals("No account found with this email", ex.getMessage());
    verify(jwtUtil, never()).generateToken(any());
}

@Test
void verifyPhoneForReset_PhoneMismatch_ThrowsBadRequestException() {
    forgotPasswordRequest.setPhoneNumber("9999999999");
    when(userRepository.findByEmail(forgotPasswordRequest.getEmail())).thenReturn(Optional.of(user));

    BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> authService.verifyPhoneForReset(forgotPasswordRequest)
    );

    assertEquals("Phone number does not match our records", ex.getMessage());
    verify(jwtUtil, never()).generateToken(any());
}

@Test
void verifyPhoneForReset_NullPhoneInUser_ThrowsBadRequestException() {
    user.setPhoneNumber(null);
    when(userRepository.findByEmail(forgotPasswordRequest.getEmail())).thenReturn(Optional.of(user));

    BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> authService.verifyPhoneForReset(forgotPasswordRequest)
    );

    assertEquals("Phone number does not match our records", ex.getMessage());
    verify(jwtUtil, never()).generateToken(any());
}

@Test
void resetPassword_Success() {
    when(userRepository.findByEmail(resetPasswordRequest.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(resetPasswordRequest.getNewPassword())).thenReturn("newEncodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(user);

    String response = authService.resetPassword(resetPasswordRequest);

    assertEquals("Password reset successfully", response);
    assertEquals("newEncodedPassword", user.getPassword());
    verify(passwordEncoder).encode(resetPasswordRequest.getNewPassword());
    verify(userRepository).save(user);
}

@Test
void resetPassword_PasswordMismatch_ThrowsBadRequestException() {
    resetPasswordRequest.setConfirmPassword("DifferentPassword@123");

    BadRequestException ex = assertThrows(
            BadRequestException.class,
            () -> authService.resetPassword(resetPasswordRequest)
    );

    assertEquals("Passwords do not match", ex.getMessage());
    verify(userRepository, never()).findByEmail(any());
}

@Test
void resetPassword_UserNotFound_ThrowsResourceNotFoundException() {
    when(userRepository.findByEmail(resetPasswordRequest.getEmail())).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(
            ResourceNotFoundException.class,
            () -> authService.resetPassword(resetPasswordRequest)
    );

    assertEquals("User not found", ex.getMessage());
    verify(passwordEncoder, never()).encode(any());
    verify(userRepository, never()).save(any());
}

}
