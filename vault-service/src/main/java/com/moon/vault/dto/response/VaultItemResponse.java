package com.moon.vault.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record VaultItemResponse(
        String itemCode,
        String folderCode,
        String title,
        String type,
        String category,
        String securityLevel,
        String description,
        Map<String, Object> metadata,
        Map<String, Object> sensitiveContent,
        LocalDate expirationDate,
        String status,
        Boolean favorite,
        List<String> tagCodes,
        LocalDateTime createDate,
        LocalDateTime updateDate
) {
}
