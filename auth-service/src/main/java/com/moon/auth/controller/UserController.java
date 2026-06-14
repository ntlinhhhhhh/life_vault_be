package com.moon.auth.controller;

import com.moon.auth.common.response.ResultResp;
import com.moon.auth.config.OpenApiConfig;
import com.moon.auth.dto.request.UpdateProfileRequest;
import com.moon.auth.dto.response.UserInfoResponse;
import com.moon.auth.security.SecurityUser;
import com.moon.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "User", description = "Current user profile APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile")
    @ApiResponse(responseCode = "200", description = "Current user returned")
    @ApiResponse(responseCode = "401", description = "Authentication is required")
    @GetMapping("/me")
    public ResultResp<UserInfoResponse> me(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResultResp.success(userService.me(securityUser));
    }

    @Operation(summary = "Update current user profile")
    @ApiResponse(responseCode = "200", description = "Profile updated")
    @ApiResponse(responseCode = "401", description = "Authentication is required")
    @PutMapping("/profile")
    public ResultResp<UserInfoResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal SecurityUser securityUser
    ) {
        return ResultResp.success(userService.updateProfile(request, securityUser));
    }
}
