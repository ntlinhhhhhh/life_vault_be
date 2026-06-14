package com.moon.vault.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdatePasswordRequest(
        @NotBlank @Size(max = 500) String title,
        @Size(max = 100) String folderCode,
        @NotBlank @Size(max = 255) String serviceName,
        @Size(max = 1000) String loginUrl,
        String username,
        String password,
        String note,
        List<String> tagCodes
) {
}
