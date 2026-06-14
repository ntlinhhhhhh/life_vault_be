package com.moon.auth.service;

import com.moon.auth.dto.response.LoginLogResponse;

import java.util.List;

public interface LoginTrackingService {

    void record(String userCode, String usernameInput, boolean success, String failureReason, String ipAddress, String userAgent);

    List<LoginLogResponse> listByUserCode(String userCode);
}
