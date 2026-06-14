package com.moon.auth.exception;

import com.moon.auth.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getRespCode();
    }

    public HttpStatus getStatus() {
        return errorCode.getHttpStatus();
    }
}
