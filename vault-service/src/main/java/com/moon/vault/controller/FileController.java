package com.moon.vault.controller;

import com.moon.vault.common.ResultResp;
import com.moon.vault.config.OpenApiConfig;
import com.moon.vault.dto.response.VaultFileResponse;
import com.moon.vault.security.CurrentUserService;
import com.moon.vault.service.FileStorageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.moon.vault.common.AppConstants.REAUTH_HEADER;

@RestController
@RequiredArgsConstructor
@Tag(name = "Files", description = "Vault file upload/download APIs")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class FileController {

    private final CurrentUserService currentUserService;
    private final FileStorageService fileStorageService;

    @PostMapping("/vault/items/{itemCode}/files")
    public ResultResp<VaultFileResponse> upload(
            @PathVariable String itemCode,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "fileType", required = false) String fileType,
            HttpServletRequest request
    ) {
        return ResultResp.success("File uploaded", fileStorageService.upload(currentUserService.userCode(request), itemCode, file, fileType, request));
    }

    @GetMapping("/vault/items/{itemCode}/files")
    public ResultResp<List<VaultFileResponse>> list(@PathVariable String itemCode, HttpServletRequest request) {
        return ResultResp.success(fileStorageService.list(currentUserService.userCode(request), itemCode));
    }

    @GetMapping("/vault/files/{fileCode}/download")
    public ResponseEntity<Resource> download(
            @PathVariable String fileCode,
            @RequestHeader(value = REAUTH_HEADER, required = false) String reauthToken,
            HttpServletRequest request
    ) {
        return fileStorageService.download(currentUserService.userCode(request), fileCode, reauthToken, request);
    }

    @DeleteMapping("/vault/files/{fileCode}")
    public ResultResp<Void> delete(@PathVariable String fileCode, HttpServletRequest request) {
        fileStorageService.delete(currentUserService.userCode(request), fileCode, request);
        return ResultResp.success("File deleted", null);
    }
}
