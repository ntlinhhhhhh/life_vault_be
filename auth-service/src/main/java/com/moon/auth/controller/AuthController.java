package com.moon.auth.controller;

import com.moon.auth.common.constant.AuthConstants;
import com.moon.auth.common.response.ResultResp;
import com.moon.auth.common.util.IpUtils;
import com.moon.auth.common.util.TokenUtils;
import com.moon.auth.config.OpenApiConfig;
import com.moon.auth.dto.request.ChangePasswordRequest;
import com.moon.auth.dto.request.LoginRequest;
import com.moon.auth.dto.request.LogoutRequest;
import com.moon.auth.dto.request.ReAuthRequest;
import com.moon.auth.dto.request.RefreshTokenRequest;
import com.moon.auth.dto.request.RegisterRequest;
import com.moon.auth.dto.response.LoginResponse;
import com.moon.auth.dto.response.ReAuthResponse;
import com.moon.auth.dto.response.RegisterResponse;
import com.moon.auth.dto.response.TokenResponse;
import com.moon.auth.security.SecurityUser;
import com.moon.auth.service.AuthService;
import com.moon.auth.service.ReAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Register, login, refresh token, logout and re-auth APIs")
public class AuthController {

    private final AuthService authService;
    private final ReAuthService reAuthService;

    @Operation(summary = "Register a new user")
    @ApiResponse(responseCode = "200", description = "User registered")
    @ApiResponse(responseCode = "400", description = "Username/email already exists or request is invalid")
    @PostMapping("/register")
    public ResultResp<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResultResp.success("Register successfully", authService.register(request));
    }

    @Operation(summary = "Login and receive access/refresh tokens")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResultResp<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ResultResp.success("Login successfully", authService.login(
                request,
                IpUtils.clientIp(httpRequest),
                httpRequest.getHeader(AuthConstants.USER_AGENT_HEADER)
        ));
    }

    @Operation(summary = "Refresh an access token")
    @ApiResponse(responseCode = "200", description = "Access token refreshed")
    @ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired")
    @PostMapping("/refresh")
    public ResultResp<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResultResp.success(authService.refresh(request));
    }

    @Operation(summary = "Logout current session", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Logout successful")
    @ApiResponse(responseCode = "401", description = "Authentication is required")
    @PostMapping("/logout")
    public ResultResp<Void> logout(
            @Valid @RequestBody LogoutRequest request,
            @RequestHeader(value = AuthConstants.AUTHORIZATION_HEADER, required = false) String authorization
    ) {
        authService.logout(request, TokenUtils.bearerToken(authorization));
        return ResultResp.success("Logout successfully", null);
    }

    @Operation(summary = "Change current user's password", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Password changed")
    @ApiResponse(responseCode = "401", description = "Authentication is required or old password is invalid")
    @PostMapping("/change-password")
    public ResultResp<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        authService.changePassword(request, securityUser);
        return ResultResp.success("Password changed successfully", null);
    }

    @Operation(summary = "Issue a short-lived re-auth token", security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH))
    @ApiResponse(responseCode = "200", description = "Re-auth token issued")
    @ApiResponse(responseCode = "401", description = "Authentication is required or password is invalid")
    @PostMapping("/re-auth")
    public ResultResp<ReAuthResponse> reAuth(
            @Valid @RequestBody ReAuthRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        return ResultResp.success(reAuthService.reAuth(request, securityUser));
    }
}
