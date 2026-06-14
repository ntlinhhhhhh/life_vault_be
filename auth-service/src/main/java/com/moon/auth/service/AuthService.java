package com.moon.auth.service;

import com.moon.auth.dto.request.ChangePasswordRequest;
import com.moon.auth.dto.request.LoginRequest;
import com.moon.auth.dto.request.LogoutRequest;
import com.moon.auth.dto.request.RefreshTokenRequest;
import com.moon.auth.dto.request.RegisterRequest;
import com.moon.auth.dto.response.LoginResponse;
import com.moon.auth.dto.response.RegisterResponse;
import com.moon.auth.dto.response.TokenResponse;
import com.moon.auth.security.SecurityUser;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);

    TokenResponse refresh(RefreshTokenRequest request);

    void logout(LogoutRequest request, String accessToken);

    void changePassword(ChangePasswordRequest request, SecurityUser securityUser);
}
