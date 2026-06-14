package com.moon.vault.common;

public final class AppConstants {

    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_ARCHIVED = "ARCHIVED";
    public static final String STATUS_DELETED = "DELETED";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_READ = "READ";
    public static final String STATUS_DISMISSED = "DISMISSED";
    public static final String STATUS_SENT = "SENT";

    public static final String SECURITY_NORMAL = "NORMAL";
    public static final String SECURITY_PRIVATE = "PRIVATE";
    public static final String SECURITY_SECRET = "SECRET";
    public static final String SECURITY_CRITICAL = "CRITICAL";

    public static final String TYPE_PASSWORD_ENTRY = "PASSWORD_ENTRY";
    public static final String CATEGORY_PASSWORD = "PASSWORD";

    public static final String TOKEN_TYPE_CLAIM = "token_type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REAUTH = "reauth";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String REAUTH_HEADER = "X-Reauth-Token";

    public static final String FOLDER_CODE_PREFIX = "FLD";
    public static final String TAG_CODE_PREFIX = "TAG";
    public static final String ITEM_CODE_PREFIX = "VIT";
    public static final String FILE_CODE_PREFIX = "FIL";
    public static final String PASSWORD_CODE_PREFIX = "PWD";
    public static final String REMINDER_CODE_PREFIX = "REM";
    public static final String AUDIT_CODE_PREFIX = "AUD";

    private AppConstants() {
    }
}
