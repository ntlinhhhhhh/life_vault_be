package com.moon.vault.exception;

import com.moon.vault.common.ErrorCode;
import lombok.Getter;

@Getter
public class VaultException extends RuntimeException {

    private final ErrorCode errorCode;

    public VaultException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static VaultException unauthorized(String message) {
        return new VaultException(ErrorCode.UNAUTHORIZED, message);
    }

    public static VaultException forbidden(String message) {
        return new VaultException(ErrorCode.FORBIDDEN, message);
    }

    public static VaultException notFound(String message) {
        return new VaultException(ErrorCode.NOT_FOUND, message);
    }

    public static VaultException badRequest(String message) {
        return new VaultException(ErrorCode.VALIDATION_ERROR, message);
    }
}
