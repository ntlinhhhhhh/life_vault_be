package com.moon.vault.controller;

import com.moon.vault.common.ResultResp;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Vault service health check")
public class HealthController {

    @GetMapping("/vault/health-check")
    public ResultResp<String> healthCheck() {
        return ResultResp.success("vault-service is up");
    }
}
