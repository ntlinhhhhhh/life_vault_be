package com.moon.auth.security;

import com.moon.auth.common.constant.AuthConstants;
import com.moon.auth.config.jwt.JwtProperties;
import com.moon.auth.entity.User;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;
    private final TokenProvider tokenProvider;

    public String generateAccessToken(User user, List<String> roles) {
        return tokenProvider.generateToken(
                user.getUserCode(),
                Map.of(
                        AuthConstants.TOKEN_TYPE_CLAIM, AuthConstants.TOKEN_TYPE_ACCESS,
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "roles", roles
                ),
                jwtProperties.getAccessTokenTtlSeconds()
        );
    }

    public String generateReauthToken(User user) {
        return tokenProvider.generateToken(
                user.getUserCode(),
                Map.of(AuthConstants.TOKEN_TYPE_CLAIM, AuthConstants.TOKEN_TYPE_REAUTH),
                jwtProperties.getReauthTokenTtlSeconds()
        );
    }

    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessToken(String token) {
        return AuthConstants.TOKEN_TYPE_ACCESS.equals(parseClaims(token).get(AuthConstants.TOKEN_TYPE_CLAIM, String.class));
    }

    public boolean isReauthToken(String token) {
        return AuthConstants.TOKEN_TYPE_REAUTH.equals(parseClaims(token).get(AuthConstants.TOKEN_TYPE_CLAIM, String.class));
    }

    public Date getExpiration(String token) {
        return parseClaims(token).getExpiration();
    }

    public long getAccessTokenTtlSeconds() {
        return jwtProperties.getAccessTokenTtlSeconds();
    }

    public long getReauthTokenTtlSeconds() {
        return jwtProperties.getReauthTokenTtlSeconds();
    }

    public boolean isValid(String token) {
        return tokenProvider.isValid(token);
    }

    public Claims parseClaims(String token) {
        return tokenProvider.parseClaims(token);
    }
}
