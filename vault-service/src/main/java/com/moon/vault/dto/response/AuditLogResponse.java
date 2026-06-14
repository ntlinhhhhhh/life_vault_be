package com.moon.vault.dto.response;

import java.time.LocalDateTime;

public record AuditLogResponse(
        String auditLogCode,
        String actionType,
        String targetType,
        String targetCode,
        String ipAddress,
        Boolean success,
        String failureReason,
        LocalDateTime createDate
) {
}
