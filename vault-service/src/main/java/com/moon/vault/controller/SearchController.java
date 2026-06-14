package com.moon.vault.controller;

import com.moon.vault.common.PageResp;
import com.moon.vault.common.ResultResp;
import com.moon.vault.config.OpenApiConfig;
import com.moon.vault.dto.request.SearchVaultItemRequest;
import com.moon.vault.dto.response.VaultItemResponse;
import com.moon.vault.security.CurrentUserService;
import com.moon.vault.service.VaultItemService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Search", description = "Vault item search APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class SearchController {

    private final CurrentUserService currentUserService;
    private final VaultItemService vaultItemService;

    @GetMapping("/vault/search")
    public ResultResp<PageResp<VaultItemResponse>> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String folderCode,
            @RequestParam(required = false) String tagCode,
            @RequestParam(required = false) String securityLevel,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean favorite,
            @RequestParam(required = false) Boolean nearExpired,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        SearchVaultItemRequest search = new SearchVaultItemRequest(q, type, category, folderCode, tagCode, securityLevel, status, favorite, nearExpired, page, size);
        return ResultResp.success(vaultItemService.search(currentUserService.userCode(request), search));
    }
}
