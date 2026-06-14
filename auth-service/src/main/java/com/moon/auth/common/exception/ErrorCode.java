package com.moon.auth.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS("00", HttpStatus.OK),
    VALIDATION_ERROR("AUTH-400", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR("AUTH-500", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("AUTH-401", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("AUTH-403", HttpStatus.FORBIDDEN),

    USERNAME_EXISTS("AUTH-1001", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTS("AUTH-1002", HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("AUTH-1003", HttpStatus.UNAUTHORIZED),
    USER_NOT_ACTIVE("AUTH-1004", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("AUTH-1005", HttpStatus.NOT_FOUND),
    INVALID_OLD_PASSWORD("AUTH-1006", HttpStatus.UNAUTHORIZED),
    INVALID_PASSWORD("AUTH-1007", HttpStatus.UNAUTHORIZED),

    INVALID_REFRESH_TOKEN("AUTH-2001", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_EXPIRED("AUTH-2002", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH-2003", HttpStatus.UNAUTHORIZED),

    SESSION_NOT_FOUND("AUTH-3001", HttpStatus.NOT_FOUND),
    SESSION_FORBIDDEN("AUTH-3002", HttpStatus.FORBIDDEN);

    private final String respCode;
    private final HttpStatus httpStatus;
}
