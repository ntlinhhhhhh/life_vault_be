package com.moon.auth.common.exception;

public final class ErrorMessage {

    public static final String SUCCESS = "Success";
    public static final String VALIDATION_ERROR = "Invalid request";
    public static final String INTERNAL_ERROR = "Internal server error";
    public static final String UNAUTHORIZED = "Authentication is required";
    public static final String ACCESS_DENIED = "Access is denied";

    public static final String USERNAME_EXISTS = "Username already exists";
    public static final String EMAIL_EXISTS = "Email already exists";
    public static final String INVALID_CREDENTIALS = "Invalid username/email or password";
    public static final String USER_NOT_ACTIVE = "User is not active";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String INVALID_OLD_PASSWORD = "Old password is invalid";
    public static final String INVALID_PASSWORD = "Password is invalid";

    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token is expired or revoked";
    public static final String SESSION_NOT_FOUND = "Session not found";
    public static final String SESSION_FORBIDDEN = "Session does not belong to current user";

    private ErrorMessage() {
    }
}
