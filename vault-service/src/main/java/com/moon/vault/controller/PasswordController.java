package com.moon.vault.controller;

import com.moon.vault.common.ResultResp;
import com.moon.vault.config.OpenApiConfig;
import com.moon.vault.dto.request.CreatePasswordRequest;
import com.moon.vault.dto.request.UpdatePasswordRequest;
import com.moon.vault.dto.response.PasswordDetailResponse;
import com.moon.vault.dto.response.PasswordRevealResponse;
import com.moon.vault.security.CurrentUserService;
import com.moon.vault.service.PasswordVaultService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.moon.vault.common.AppConstants.REAUTH_HEADER;

@RestController
@RequestMapping("/vault/passwords")
@RequiredArgsConstructor
@Tag(name = "Passwords", description = "Encrypted password vault APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class PasswordController {

    private final CurrentUserService currentUserService;
    private final PasswordVaultService passwordVaultService;

    @PostMapping
    public ResultResp<PasswordDetailResponse> create(@Valid @RequestBody CreatePasswordRequest body, HttpServletRequest request) {
        return ResultResp.success("Password entry created", passwordVaultService.create(currentUserService.userCode(request), body, request));
    }

    @GetMapping("/{itemCode}")
    public ResultResp<PasswordDetailResponse> detail(@PathVariable String itemCode, HttpServletRequest request) {
        return ResultResp.success(passwordVaultService.detail(currentUserService.userCode(request), itemCode, request));
    }

    @PostMapping("/{itemCode}/reveal")
    public ResultResp<PasswordRevealResponse> reveal(
            @PathVariable String itemCode,
            @RequestHeader(value = REAUTH_HEADER, required = false) String reauthToken,
            HttpServletRequest request
    ) {
        return ResultResp.success(passwordVaultService.reveal(currentUserService.userCode(request), itemCode, reauthToken, request));
    }

    @PutMapping("/{itemCode}")
    public ResultResp<PasswordDetailResponse> update(
            @PathVariable String itemCode,
            @Valid @RequestBody UpdatePasswordRequest body,
            HttpServletRequest request
    ) {
        return ResultResp.success(passwordVaultService.update(currentUserService.userCode(request), itemCode, body, request));
    }

    @PostMapping("/{itemCode}/copy")
    public ResultResp<PasswordRevealResponse> copy(
            @PathVariable String itemCode,
            @RequestHeader(value = REAUTH_HEADER, required = false) String reauthToken,
            HttpServletRequest request
    ) {
        return ResultResp.success(passwordVaultService.copy(currentUserService.userCode(request), itemCode, reauthToken, request));
    }
}
