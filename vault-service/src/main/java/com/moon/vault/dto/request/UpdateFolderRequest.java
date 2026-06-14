package com.moon.vault.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateFolderRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 100) String parentFolderCode,
        Integer sortOrder
) {
}
