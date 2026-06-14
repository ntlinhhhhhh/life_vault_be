package com.moon.vault.security;

import com.moon.vault.common.AppConstants;
import com.moon.vault.exception.VaultException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtService {

    @Value("${vault.jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("vault.jwt.secret must be at least 32 bytes");
        }
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String subjectFromAccessToken(String authorization) {
        String token = bearerToken(authorization);
        if (token == null) {
            throw VaultException.unauthorized("Authentication is required");
        }
        Claims claims = parse(token);
        if (!AppConstants.TOKEN_TYPE_ACCESS.equals(claims.get(AppConstants.TOKEN_TYPE_CLAIM, String.class))) {
            throw VaultException.unauthorized("Access token is invalid");
        }
        return claims.getSubject();
    }

    public void validateReauthToken(String reauthToken, String expectedUserCode) {
        if (reauthToken == null || reauthToken.isBlank()) {
            throw VaultException.unauthorized("Re-auth token is required");
        }
        Claims claims = parse(reauthToken);
        if (!AppConstants.TOKEN_TYPE_REAUTH.equals(claims.get(AppConstants.TOKEN_TYPE_CLAIM, String.class))
                || !expectedUserCode.equals(claims.getSubject())) {
            throw VaultException.unauthorized("Re-auth token is invalid");
        }
    }

    private Claims parse(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException exception) {
            throw VaultException.unauthorized("Token is invalid or expired");
        }
    }

    private String bearerToken(String authorization) {
        if (authorization == null || authorization.isBlank() || !authorization.startsWith(AppConstants.BEARER_PREFIX)) {
            return null;
        }
        return authorization.substring(AppConstants.BEARER_PREFIX.length());
    }
}
