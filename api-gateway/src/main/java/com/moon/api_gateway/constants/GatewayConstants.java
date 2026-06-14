package com.moon.api_gateway.constants;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;

import java.util.Set;

public final class GatewayConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String REDIS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh"
    );

    private GatewayConstants() {
    }

    public static boolean isPublicRequest(HttpServletRequest request) {
        String path = normalizePath(request.getRequestURI().substring(request.getContextPath().length()));
        return HttpMethod.OPTIONS.matches(request.getMethod())
                || path.startsWith("/actuator")
                || PUBLIC_PATHS.contains(path);
    }

    public static boolean isGatewayManagedPath(HttpServletRequest request) {
        String path = normalizePath(request.getRequestURI().substring(request.getContextPath().length()));
        return path.startsWith("/api/auth") || path.startsWith("/api/vault");
    }

    public static String bearerToken(String authorization) {
        if (authorization == null || authorization.isBlank() || !authorization.startsWith(BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(BEARER_PREFIX.length());
    }

    private static String normalizePath(String path) {
        if (path.length() > 1 && path.endsWith("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
