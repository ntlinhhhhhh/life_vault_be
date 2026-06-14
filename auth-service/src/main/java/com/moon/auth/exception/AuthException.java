package com.moon.auth.exception;

import com.moon.auth.common.exception.ErrorCode;

public class AuthException extends CustomException {

    public AuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static AuthException badRequest(ErrorCode errorCode, String message) {
        return new AuthException(errorCode, message);
    }

    public static AuthException unauthorized(ErrorCode errorCode, String message) {
        return new AuthException(errorCode, message);
    }

    public static AuthException forbidden(ErrorCode errorCode, String message) {
        return new AuthException(errorCode, message);
    }

    public static AuthException notFound(ErrorCode errorCode, String message) {
        return new AuthException(errorCode, message);
    }
}
