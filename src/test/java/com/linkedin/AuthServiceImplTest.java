package com.linkedin;

import com.linkedin.dto.request.LoginRequest;
import com.linkedin.dto.request.RegisterRequest;
import com.linkedin.dto.response.AuthResponse;
import com.linkedin.entity.User;
import com.linkedin.exception.BadRequestException;
import com.linkedin.repository.UserRepository;
import com.linkedin.security.JwtUtil;
import com.linkedin.serviceImpl.AuthServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private ModelMapper modelMapper;

    @InjectMocks private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
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
                .build();

        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .isActive(true)
                .build();
    }

    @Test
    void register_Success() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(user);
        when(jwtUtil.generateToken(any())).thenReturn("testToken");
        when(modelMapper.map(any(), any())).thenReturn(null);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyExists_ThrowsBadRequest() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_PasswordMismatch_ThrowsBadRequest() {
        registerRequest.setConfirmPassword("DifferentPassword@123");

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("john.doe@example.com")
                .password("Password@123")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any())).thenReturn("testToken");
        when(modelMapper.map(any(), any())).thenReturn(null);

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        verify(jwtUtil).generateToken(user.getEmail());
    }

    @Test
    void login_InvalidEmail_ThrowsBadRequest() {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("wrong@example.com")
                .password("Password@123")
                .build();

        when(authenticationManager.authenticate(any())).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
    }
}
