package com.moon.vault.service;

import com.moon.vault.dto.response.VaultFileResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    VaultFileResponse upload(String userCode, String itemCode, MultipartFile file, String fileType, HttpServletRequest httpRequest);

    List<VaultFileResponse> list(String userCode, String itemCode);

    ResponseEntity<Resource> download(String userCode, String fileCode, String reauthToken, HttpServletRequest httpRequest);

    void delete(String userCode, String fileCode, HttpServletRequest httpRequest);
}
