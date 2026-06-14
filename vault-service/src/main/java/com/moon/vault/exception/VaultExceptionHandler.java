package com.moon.vault.exception;

import com.moon.vault.common.ErrorCode;
import com.moon.vault.common.ResultResp;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class VaultExceptionHandler {

    @ExceptionHandler(VaultException.class)
    public ResponseEntity<ResultResp<Void>> handleVaultException(VaultException exception) {
        return ResponseEntity
                .status(exception.getErrorCode().getHttpStatus())
                .body(ResultResp.error(exception.getErrorCode(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultResp<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse("Request is invalid");
        return ResponseEntity.badRequest().body(ResultResp.error(ErrorCode.VALIDATION_ERROR, message));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ResultResp<Void>> handleMaxUpload(MaxUploadSizeExceededException exception) {
        return ResponseEntity
                .status(ErrorCode.FILE_TOO_LARGE.getHttpStatus())
                .body(ResultResp.error(ErrorCode.FILE_TOO_LARGE, "File is too large"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultResp<Void>> handleException(Exception exception) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
                .body(ResultResp.error(ErrorCode.INTERNAL_ERROR, "Vault internal error"));
    }
}
