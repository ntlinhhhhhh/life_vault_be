package com.moon.auth.service;

import java.time.Duration;

public interface RedisService {

    void blacklistToken(String token, Duration ttl);

    boolean isTokenBlacklisted(String token);
}
