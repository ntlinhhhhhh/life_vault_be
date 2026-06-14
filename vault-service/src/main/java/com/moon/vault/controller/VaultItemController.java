package com.moon.vault.controller;

import com.moon.vault.common.PageResp;
import com.moon.vault.common.ResultResp;
import com.moon.vault.config.OpenApiConfig;
import com.moon.vault.dto.request.CreateVaultItemRequest;
import com.moon.vault.dto.request.RenewItemRequest;
import com.moon.vault.dto.request.SearchVaultItemRequest;
import com.moon.vault.dto.request.UpdateVaultItemRequest;
import com.moon.vault.dto.response.VaultItemResponse;
import com.moon.vault.security.CurrentUserService;
import com.moon.vault.service.VaultItemService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.moon.vault.common.AppConstants.REAUTH_HEADER;

@RestController
@RequestMapping("/vault/items")
@RequiredArgsConstructor
@Tag(name = "Vault Items", description = "Vault item CRUD, lifecycle and renewal APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class VaultItemController {

    private final CurrentUserService currentUserService;
    private final VaultItemService vaultItemService;

    @GetMapping
    public ResultResp<PageResp<VaultItemResponse>> list(
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

    @PostMapping
    public ResultResp<VaultItemResponse> create(@Valid @RequestBody CreateVaultItemRequest body, HttpServletRequest request) {
        return ResultResp.success("Item created", vaultItemService.create(currentUserService.userCode(request), body, request));
    }

    @GetMapping("/{itemCode}")
    public ResultResp<VaultItemResponse> detail(@PathVariable String itemCode, HttpServletRequest request) {
        return ResultResp.success(vaultItemService.detail(currentUserService.userCode(request), itemCode, request));
    }

    @PutMapping("/{itemCode}")
    public ResultResp<VaultItemResponse> update(
            @PathVariable String itemCode,
            @Valid @RequestBody UpdateVaultItemRequest body,
            HttpServletRequest request
    ) {
        return ResultResp.success(vaultItemService.update(currentUserService.userCode(request), itemCode, body, request));
    }

    @DeleteMapping("/{itemCode}")
    public ResultResp<Void> delete(@PathVariable String itemCode, HttpServletRequest request) {
        vaultItemService.delete(currentUserService.userCode(request), itemCode, request);
        return ResultResp.success("Item deleted", null);
    }

    @PostMapping("/{itemCode}/restore")
    public ResultResp<VaultItemResponse> restore(@PathVariable String itemCode, HttpServletRequest request) {
        return ResultResp.success(vaultItemService.restore(currentUserService.userCode(request), itemCode, request));
    }

    @DeleteMapping("/{itemCode}/purge")
    public ResultResp<Void> purge(
            @PathVariable String itemCode,
            @RequestHeader(value = REAUTH_HEADER, required = false) String reauthToken,
            HttpServletRequest request
    ) {
        vaultItemService.purge(currentUserService.userCode(request), itemCode, reauthToken, request);
        return ResultResp.success("Item purged", null);
    }

    @PostMapping("/{itemCode}/favorite")
    public ResultResp<VaultItemResponse> favorite(@PathVariable String itemCode, HttpServletRequest request) {
        return ResultResp.success(vaultItemService.toggleFavorite(currentUserService.userCode(request), itemCode, request));
    }

    @PostMapping("/{itemCode}/archive")
    public ResultResp<VaultItemResponse> archive(@PathVariable String itemCode, HttpServletRequest request) {
        return ResultResp.success(vaultItemService.archive(currentUserService.userCode(request), itemCode, request));
    }

    @PostMapping("/{itemCode}/renew")
    public ResultResp<VaultItemResponse> renew(
            @PathVariable String itemCode,
            @Valid @RequestBody RenewItemRequest body,
            HttpServletRequest request
    ) {
        return ResultResp.success(vaultItemService.renew(currentUserService.userCode(request), itemCode, body, request));
    }
}
