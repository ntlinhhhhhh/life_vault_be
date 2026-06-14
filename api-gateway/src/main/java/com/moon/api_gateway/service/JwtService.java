package com.moon.api_gateway.service;

import com.moon.api_gateway.constants.GatewayConstants;
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

    @Value("${gateway.jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("gateway.jwt.secret must be at least 32 bytes");
        }
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!GatewayConstants.TOKEN_TYPE_ACCESS.equals(claims.get(GatewayConstants.TOKEN_TYPE_CLAIM, String.class))) {
                throw new JwtException("JWT token_type is not access");
            }
            return claims;
        } catch (JwtException | IllegalArgumentException exception) {
            throw exception;
        }
    }
}
