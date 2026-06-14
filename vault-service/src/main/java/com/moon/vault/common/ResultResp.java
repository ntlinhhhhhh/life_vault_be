package com.moon.vault.common;

public record ResultResp<T>(String respCode, String message, T data) {

    public static <T> ResultResp<T> success(T data) {
        return new ResultResp<>(ErrorCode.SUCCESS.getRespCode(), "Success", data);
    }

    public static <T> ResultResp<T> success(String message, T data) {
        return new ResultResp<>(ErrorCode.SUCCESS.getRespCode(), message, data);
    }

    public static <T> ResultResp<T> error(ErrorCode errorCode, String message) {
        return new ResultResp<>(errorCode.getRespCode(), message, null);
    }
}
