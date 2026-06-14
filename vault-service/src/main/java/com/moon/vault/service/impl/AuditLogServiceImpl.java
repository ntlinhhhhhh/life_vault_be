package com.moon.vault.service.impl;

import com.moon.vault.common.AppConstants;
import com.moon.vault.common.PageResp;
import com.moon.vault.dto.response.AuditLogResponse;
import com.moon.vault.entity.AuditLog;
import com.moon.vault.repository.AuditLogRepository;
import com.moon.vault.service.AuditLogService;
import com.moon.vault.util.CodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String userCode, String actionType, String targetType, String targetCode, HttpServletRequest request) {
        AuditLog log = new AuditLog();
        log.setAuditLogCode(CodeGenerator.generate(AppConstants.AUDIT_CODE_PREFIX));
        log.setUserCode(userCode);
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetCode(targetCode);
        log.setIpAddress(clientIp(request));
        log.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
        log.setSuccess(true);
        log.setCreateUser(userCode);
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResp<AuditLogResponse> list(String userCode, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        var result = auditLogRepository.findByUserCodeOrderByCreateDateDesc(userCode, pageable);
        return new PageResp<>(
                result.getContent().stream().map(this::toResponse).toList(),
                safePage,
                safeSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getAuditLogCode(),
                log.getActionType(),
                log.getTargetType(),
                log.getTargetCode(),
                log.getIpAddress(),
                log.getSuccess(),
                log.getFailureReason(),
                log.getCreateDate()
        );
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
