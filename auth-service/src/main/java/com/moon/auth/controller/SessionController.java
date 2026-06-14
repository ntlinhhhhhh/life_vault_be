package com.moon.auth.controller;

import com.moon.auth.common.response.ResultResp;
import com.moon.auth.config.OpenApiConfig;
import com.moon.auth.dto.response.SessionResponse;
import com.moon.auth.security.SecurityUser;
import com.moon.auth.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auth/sessions")
@RequiredArgsConstructor
@Tag(name = "Sessions", description = "Logged-in device/session management APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class SessionController {

    private final SessionService sessionService;

    @Operation(summary = "List current user's active sessions")
    @ApiResponse(responseCode = "200", description = "Sessions returned")
    @ApiResponse(responseCode = "401", description = "Authentication is required")
    @GetMapping
    public ResultResp<List<SessionResponse>> listSessions(@AuthenticationPrincipal SecurityUser securityUser) {
        return ResultResp.success(sessionService.listSessions(securityUser));
    }

    @Operation(summary = "Revoke one session by session code")
    @ApiResponse(responseCode = "200", description = "Session revoked")
    @ApiResponse(responseCode = "401", description = "Authentication is required")
    @ApiResponse(responseCode = "403", description = "Session does not belong to current user")
    @ApiResponse(responseCode = "404", description = "Session not found")
    @PostMapping("/{sessionCode}/revoke")
    public ResultResp<Void> revokeSession(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable String sessionCode
    ) {
        sessionService.revokeSession(securityUser, sessionCode);
        return ResultResp.success("Session revoked successfully", null);
    }
}
