package com.moon.auth.exception;

import com.moon.auth.common.exception.ErrorCode;

public class TokenException extends CustomException {

    public TokenException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
