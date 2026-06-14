package com.moon.api_gateway.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GatewayException extends RuntimeException {

    private final HttpStatus status;
    private final String respCode;

    public GatewayException(HttpStatus status, String respCode, String message) {
        super(message);
        this.status = status;
        this.respCode = respCode;
    }
}
