package com.moon.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class LoginLogResponse {

    private String loginLogCode;
    private String userCode;
    private String usernameInput;
    private Boolean success;
    private String failureReason;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createDate;
}
