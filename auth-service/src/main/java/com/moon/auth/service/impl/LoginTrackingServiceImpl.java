package com.moon.auth.service.impl;

import com.moon.auth.common.constant.AppConstants;
import com.moon.auth.common.util.TokenUtils;
import com.moon.auth.dto.response.LoginLogResponse;
import com.moon.auth.entity.LoginLog;
import com.moon.auth.mapper.LoginLogMapper;
import com.moon.auth.repository.LoginLogRepository;
import com.moon.auth.service.LoginTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginTrackingServiceImpl implements LoginTrackingService {

    private final LoginLogRepository loginLogRepository;
    private final LoginLogMapper loginLogMapper;

    @Override
    @Transactional
    public void record(
            String userCode,
            String usernameInput,
            boolean success,
            String failureReason,
            String ipAddress,
            String userAgent
    ) {
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginLogCode(TokenUtils.generateCode(AppConstants.LOGIN_LOG_CODE_PREFIX));
        loginLog.setUserCode(userCode);
        loginLog.setUsernameInput(usernameInput);
        loginLog.setSuccess(success);
        loginLog.setFailureReason(failureReason);
        loginLog.setIpAddress(ipAddress);
        loginLog.setUserAgent(userAgent);
        loginLog.setCreateUser(userCode == null ? AppConstants.ANONYMOUS_USER : userCode);
        loginLogRepository.save(loginLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginLogResponse> listByUserCode(String userCode) {
        return loginLogRepository.findByUserCodeOrderByCreateDateDesc(userCode).stream()
                .map(loginLogMapper::toResponse)
                .toList();
    }
}
