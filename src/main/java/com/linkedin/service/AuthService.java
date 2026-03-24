package com.linkedin.service;

import com.linkedin.dto.request.ForgotPasswordRequest;
import com.linkedin.dto.request.LoginRequest;
import com.linkedin.dto.request.RegisterRequest;
import com.linkedin.dto.request.ResetPasswordRequest;
import com.linkedin.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    String verifyPhoneForReset(ForgotPasswordRequest request);
    String resetPassword(ResetPasswordRequest request);
}
