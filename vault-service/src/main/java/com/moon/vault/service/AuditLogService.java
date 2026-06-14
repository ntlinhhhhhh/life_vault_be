package com.moon.vault.service;

import com.moon.vault.common.PageResp;
import com.moon.vault.dto.response.AuditLogResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuditLogService {

    void record(String userCode, String actionType, String targetType, String targetCode, HttpServletRequest request);

    PageResp<AuditLogResponse> list(String userCode, int page, int size);
}
