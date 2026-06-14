package com.moon.vault.dto.response;

public record PasswordDetailResponse(
        String itemCode,
        String passwordEntryCode,
        String serviceName,
        String loginUrl,
        String usernameMasked,
        String passwordMasked,
        String securityLevel
) {
}
