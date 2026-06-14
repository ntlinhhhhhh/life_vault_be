package com.moon.vault.dto.response;

import java.time.LocalDateTime;

public record FolderResponse(
        String folderCode,
        String parentFolderCode,
        String name,
        String path,
        Integer sortOrder,
        LocalDateTime createDate,
        LocalDateTime updateDate
) {
}
