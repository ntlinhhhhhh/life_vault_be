package com.moon.api_gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moon.api_gateway.constants.GatewayConstants;
import com.moon.api_gateway.exception.GatewayErrorResponse;
import com.moon.api_gateway.service.JwtBlacklistService;
import com.moon.api_gateway.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@RequiredArgsConstructor
public class JwtGlobalFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtBlacklistService jwtBlacklistService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!GatewayConstants.isGatewayManagedPath(request) || GatewayConstants.isPublicRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = GatewayConstants.bearerToken(request.getHeader(GatewayConstants.AUTHORIZATION_HEADER));
        if (token == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "GW-401", "Authentication is required");
            return;
        }

        try {
            jwtService.parseAccessToken(token);
        } catch (JwtException | IllegalArgumentException exception) {
            writeError(response, HttpStatus.UNAUTHORIZED, "GW-401", "Access token is invalid or expired");
            return;
        }

        if (jwtBlacklistService.isBlacklisted(token)) {
            writeError(response, HttpStatus.UNAUTHORIZED, "GW-401", "Access token has been revoked");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), new GatewayErrorResponse(code, message, null));
    }
}
