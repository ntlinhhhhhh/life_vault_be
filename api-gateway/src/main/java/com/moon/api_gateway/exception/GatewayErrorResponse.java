package com.moon.api_gateway.exception;

public record GatewayErrorResponse(String respCode, String message, Object data) {
}
