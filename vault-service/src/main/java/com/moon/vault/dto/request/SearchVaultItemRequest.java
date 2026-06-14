package com.moon.vault.dto.request;

public record SearchVaultItemRequest(
        String q,
        String type,
        String category,
        String folderCode,
        String tagCode,
        String securityLevel,
        String status,
        Boolean favorite,
        Boolean nearExpired,
        int page,
        int size
) {
}
