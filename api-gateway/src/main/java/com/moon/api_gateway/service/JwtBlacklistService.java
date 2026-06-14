package com.moon.api_gateway.service;

import com.moon.api_gateway.constants.GatewayConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtBlacklistService {

    private final StringRedisTemplate redisTemplate;

    @Value("${gateway.redis.blacklist-enabled:true}")
    private boolean blacklistEnabled;

    @Value("${gateway.redis.failure-backoff-seconds:30}")
    private long failureBackoffSeconds;

    private volatile Instant skipUntil = Instant.EPOCH;

    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank() || shouldSkipRedis()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(GatewayConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token));
        } catch (RuntimeException ignored) {
            skipUntil = Instant.now().plusSeconds(failureBackoffSeconds);
            return false;
        }
    }

    private boolean shouldSkipRedis() {
        return !blacklistEnabled || Instant.now().isBefore(skipUntil);
    }
}
