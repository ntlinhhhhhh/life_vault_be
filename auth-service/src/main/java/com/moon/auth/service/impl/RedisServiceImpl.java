package com.moon.auth.service.impl;

import com.moon.auth.common.constant.AuthConstants;
import com.moon.auth.common.util.StringUtils;
import com.moon.auth.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redisTemplate;

    @Value("${auth.redis.blacklist-enabled:true}")
    private boolean blacklistEnabled;

    @Value("${auth.redis.failure-backoff-seconds:30}")
    private long failureBackoffSeconds;

    private volatile Instant skipUntil = Instant.EPOCH;

    @Override
    public void blacklistToken(String token, Duration ttl) {
        if (!StringUtils.hasText(token) || ttl.isNegative() || ttl.isZero() || shouldSkipRedis()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(AuthConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token, "1", ttl);
        } catch (RuntimeException ignored) {
            // Redis may be unavailable during local development; DB session state is still persisted.
            markRedisUnavailable();
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        if (!StringUtils.hasText(token) || shouldSkipRedis()) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(AuthConstants.REDIS_TOKEN_BLACKLIST_PREFIX + token));
        } catch (RuntimeException ignored) {
            markRedisUnavailable();
            return false;
        }
    }

    private boolean shouldSkipRedis() {
        return !blacklistEnabled || Instant.now().isBefore(skipUntil);
    }

    private void markRedisUnavailable() {
        skipUntil = Instant.now().plusSeconds(failureBackoffSeconds);
    }
}
