package com.moon.auth.common.constant;

public final class AuthConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String USER_AGENT_HEADER = "User-Agent";
    public static final String X_FORWARDED_FOR_HEADER = "X-Forwarded-For";

    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REAUTH = "reauth";

    public static final String REDIS_TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";

    private AuthConstants() {
    }
}
