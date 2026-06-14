package com.moon.vault.dto.response;

import java.time.LocalDateTime;

public record VaultFileResponse(
        String fileCode,
        String vaultItemCode,
        String originalFilename,
        String mimeType,
        Long fileSize,
        String checksum,
        Boolean encrypted,
        String status,
        LocalDateTime createDate
) {
}
