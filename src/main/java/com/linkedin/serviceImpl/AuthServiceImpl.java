package com.linkedin.serviceImpl;

import com.linkedin.dto.request.ForgotPasswordRequest;
import com.linkedin.dto.request.LoginRequest;
import com.linkedin.dto.request.RegisterRequest;
import com.linkedin.dto.request.ResetPasswordRequest;
import com.linkedin.dto.response.AuthResponse;
import com.linkedin.dto.response.UserResponse;
import com.linkedin.entity.User;
import com.linkedin.exception.BadRequestException;
import com.linkedin.exception.ResourceNotFoundException;
import com.linkedin.exception.UnauthorizedException;
import com.linkedin.repository.UserRepository;
import com.linkedin.security.JwtUtil;
import com.linkedin.service.AuthService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("This email is already associated with an existing account");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .profilePhotoUrl(request.getProfilePhotoUrl())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: {}", user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail());
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);

        return AuthResponse.builder()
                .token(token).tokenType("Bearer").user(userResponse).build();
    }

    /**
     * US02: Login protected by Resilience4j circuit breaker named "loginService".
     * After 3 consecutive failures the circuit opens for 60 seconds.
     * While open, loginFallback() is called immediately without hitting the DB.
     */
    @Override
    @CircuitBreaker(name = "loginService", fallbackMethod = "loginFallback")
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            // Re-throw so Resilience4j counts this as a failure
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        String token = jwtUtil.generateToken(user.getEmail());
        UserResponse userResponse = modelMapper.map(user, UserResponse.class);

        log.info("User logged in: {}", user.getEmail());
        return AuthResponse.builder()
                .token(token).tokenType("Bearer").user(userResponse).build();
    }

    /**
     * Fallback when circuit is OPEN (too many failed login attempts).
     * Returns a special marker so the controller can respond with 423 Locked.
     */
    public AuthResponse loginFallback(LoginRequest request, CallNotPermittedException ex) {
        log.warn("Login circuit OPEN for email: {} — {}", request.getEmail(), ex.getMessage());
        throw new UnauthorizedException("CIRCUIT_OPEN: Too many failed login attempts. Please wait 60 seconds before trying again.");
    }

    public AuthResponse loginFallback(LoginRequest request, Exception ex) {
        log.error("Login fallback triggered: {}", ex.getMessage());
        throw new UnauthorizedException(ex.getMessage());
    }

    @Override
    public String verifyPhoneForReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        if (user.getPhoneNumber() == null || !user.getPhoneNumber().equals(request.getPhoneNumber())) {
            throw new BadRequestException("Phone number does not match our records");
        }

        String resetToken = jwtUtil.generateToken(user.getEmail());
        log.info("Password reset verified for: {}", user.getEmail());
        return resetToken;
    }

    @Override
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password reset for: {}", user.getEmail());
        return "Password reset successfully";
    }
}
