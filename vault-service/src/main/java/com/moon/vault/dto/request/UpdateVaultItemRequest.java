package com.moon.vault.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record UpdateVaultItemRequest(
        @NotBlank @Size(max = 500) String title,
        @NotBlank @Size(max = 50) String type,
        @NotBlank @Size(max = 50) String category,
        @Size(max = 100) String folderCode,
        @Size(max = 30) String securityLevel,
        String description,
        Map<String, Object> metadata,
        Map<String, Object> sensitiveContent,
        LocalDate expirationDate,
        List<String> tagCodes
) {
}
