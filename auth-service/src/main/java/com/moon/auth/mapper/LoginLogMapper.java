package com.moon.auth.mapper;

import com.moon.auth.dto.response.LoginLogResponse;
import com.moon.auth.entity.LoginLog;
import org.springframework.stereotype.Component;

@Component
public class LoginLogMapper {

    public LoginLogResponse toResponse(LoginLog loginLog) {
        return new LoginLogResponse(
                loginLog.getLoginLogCode(),
                loginLog.getUserCode(),
                loginLog.getUsernameInput(),
                loginLog.getSuccess(),
                loginLog.getFailureReason(),
                loginLog.getIpAddress(),
                loginLog.getUserAgent(),
                loginLog.getCreateDate()
        );
    }
}
