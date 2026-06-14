package com.moon.auth.common.response;

import com.moon.auth.common.exception.ErrorCode;
import com.moon.auth.common.exception.ErrorMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResultResp<T> {

    private String respCode;
    private String message;
    private T data;

    public static <T> ResultResp<T> success(T data) {
        return new ResultResp<>(ErrorCode.SUCCESS.getRespCode(), ErrorMessage.SUCCESS, data);
    }

    public static <T> ResultResp<T> success(String message, T data) {
        return new ResultResp<>(ErrorCode.SUCCESS.getRespCode(), message, data);
    }

    public static <T> ResultResp<T> error(ErrorCode errorCode, String message) {
        return new ResultResp<>(errorCode.getRespCode(), message, null);
    }
}
