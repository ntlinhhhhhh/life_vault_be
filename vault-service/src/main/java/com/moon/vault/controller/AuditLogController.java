package com.moon.vault.controller;

import com.moon.vault.common.PageResp;
import com.moon.vault.common.ResultResp;
import com.moon.vault.config.OpenApiConfig;
import com.moon.vault.dto.response.AuditLogResponse;
import com.moon.vault.security.CurrentUserService;
import com.moon.vault.service.AuditLogService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vault/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "User audit log APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AuditLogController {

    private final CurrentUserService currentUserService;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResultResp<PageResp<AuditLogResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        return ResultResp.success(auditLogService.list(currentUserService.userCode(request), page, size));
    }
}
