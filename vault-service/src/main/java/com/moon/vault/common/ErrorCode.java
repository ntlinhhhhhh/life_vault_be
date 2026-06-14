package com.moon.vault.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    SUCCESS("00", HttpStatus.OK),
    VALIDATION_ERROR("VAULT-400", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("VAULT-401", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("VAULT-403", HttpStatus.FORBIDDEN),
    NOT_FOUND("VAULT-404", HttpStatus.NOT_FOUND),
    CONFLICT("VAULT-409", HttpStatus.CONFLICT),
    FILE_TOO_LARGE("VAULT-413", HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_FILE_TYPE("VAULT-415", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INTERNAL_ERROR("VAULT-500", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String respCode;
    private final HttpStatus httpStatus;
}
