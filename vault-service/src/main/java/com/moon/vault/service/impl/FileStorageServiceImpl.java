package com.moon.vault.service.impl;

import com.moon.vault.common.AppConstants;
import com.moon.vault.common.ErrorCode;
import com.moon.vault.dto.response.VaultFileResponse;
import com.moon.vault.entity.VaultFile;
import com.moon.vault.entity.VaultItem;
import com.moon.vault.exception.VaultException;
import com.moon.vault.repository.VaultFileRepository;
import com.moon.vault.repository.VaultItemRepository;
import com.moon.vault.security.JwtService;
import com.moon.vault.service.AuditLogService;
import com.moon.vault.service.EncryptionService;
import com.moon.vault.service.FileStorageService;
import com.moon.vault.service.ObjectStorageService;
import com.moon.vault.util.ChecksumUtil;
import com.moon.vault.util.CodeGenerator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageServiceImpl implements FileStorageService {

    private final VaultItemRepository vaultItemRepository;
    private final VaultFileRepository vaultFileRepository;
    private final EncryptionService encryptionService;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final ObjectStorageService objectStorageService;

    @Value("${vault.storage.max-file-size-bytes:10485760}")
    private long maxFileSizeBytes;

    @Value("${vault.storage.allowed-mime-types:image/jpeg,image/png,application/pdf,text/plain,application/octet-stream}")
    private Set<String> allowedMimeTypes;

    @Override
    @Transactional
    public VaultFileResponse upload(String userCode, String itemCode, MultipartFile file, String fileType, HttpServletRequest httpRequest) {
        VaultItem item = getItem(userCode, itemCode);
        validate(file);
        try {
            byte[] originalBytes = file.getBytes();
            String checksum = ChecksumUtil.sha256(originalBytes);
            boolean encrypt = requiresExtraProtection(item);
            byte[] storedBytes = originalBytes;
            String iv = null;
            if (encrypt) {
                var encrypted = encryptionService.encryptBytes(originalBytes);
                storedBytes = encrypted.ciphertext();
                iv = encrypted.iv();
            }

            String fileCode = CodeGenerator.generate(AppConstants.FILE_CODE_PREFIX);
            String storedFilename = fileCode + "-" + UUID.randomUUID();
            String objectKey = userCode + "/" + itemCode + "/" + storedFilename;
            String contentType = file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType();
            objectStorageService.put(objectKey, storedBytes, contentType);

            VaultFile vaultFile = new VaultFile();
            vaultFile.setFileCode(fileCode);
            vaultFile.setVaultItemCode(itemCode);
            vaultFile.setUserCode(userCode);
            vaultFile.setOriginalFilename(safeFilename(file.getOriginalFilename()));
            vaultFile.setStoredFilename(storedFilename);
            vaultFile.setFilePath(objectKey);
            vaultFile.setMimeType(contentType);
            vaultFile.setFileSize(file.getSize());
            vaultFile.setChecksum(checksum);
            vaultFile.setEncrypted(encrypt);
            vaultFile.setEncryptionIv(iv);
            vaultFile.setStatus(AppConstants.STATUS_ACTIVE);
            vaultFile.setCreateUser(userCode);
            vaultFile = vaultFileRepository.save(vaultFile);
            auditLogService.record(userCode, "UPLOAD_FILE", "FILE", fileCode, httpRequest);
            return toResponse(vaultFile);
        } catch (VaultException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new VaultException(ErrorCode.INTERNAL_ERROR, "File upload failed");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaultFileResponse> list(String userCode, String itemCode) {
        getItem(userCode, itemCode);
        return vaultFileRepository
                .findByVaultItemCodeAndUserCodeAndStatusOrderByCreateDateDesc(itemCode, userCode, AppConstants.STATUS_ACTIVE)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ResponseEntity<Resource> download(String userCode, String fileCode, String reauthToken, HttpServletRequest httpRequest) {
        VaultFile vaultFile = getFile(userCode, fileCode);
        VaultItem item = getItem(userCode, vaultFile.getVaultItemCode());
        if (requiresExtraProtection(item)) {
            jwtService.validateReauthToken(reauthToken, userCode);
        }
        try {
            byte[] storedBytes = objectStorageService.get(vaultFile.getFilePath());
            byte[] responseBytes = Boolean.TRUE.equals(vaultFile.getEncrypted())
                    ? encryptionService.decryptBytes(storedBytes, vaultFile.getEncryptionIv())
                    : storedBytes;
            auditLogService.record(userCode, "DOWNLOAD_FILE", "FILE", fileCode, httpRequest);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(vaultFile.getMimeType()))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment().filename(vaultFile.getOriginalFilename()).build().toString()
                    )
                    .body(new ByteArrayResource(responseBytes));
        } catch (VaultException exception) {
            throw exception;
        } catch (Exception exception) {
            throw VaultException.notFound("File content not found");
        }
    }

    @Override
    @Transactional
    public void delete(String userCode, String fileCode, HttpServletRequest httpRequest) {
        VaultFile vaultFile = getFile(userCode, fileCode);
        vaultFile.setStatus(AppConstants.STATUS_DELETED);
        vaultFile.setDeletedAt(java.time.LocalDateTime.now());
        vaultFile.setUpdateUser(userCode);
        vaultFileRepository.save(vaultFile);
        auditLogService.record(userCode, "DELETE_FILE", "FILE", fileCode, httpRequest);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw VaultException.badRequest("File is required");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new VaultException(ErrorCode.FILE_TOO_LARGE, "File is too large");
        }
        String contentType = file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType();
        if (!allowedMimeTypes.contains(contentType)) {
            throw new VaultException(ErrorCode.INVALID_FILE_TYPE, "File type is not allowed");
        }
    }

    private VaultItem getItem(String userCode, String itemCode) {
        return vaultItemRepository.findByVaultItemCodeAndUserCode(itemCode, userCode)
                .filter(item -> !"deleted".equalsIgnoreCase(item.getWorkflowState()))
                .orElseThrow(() -> VaultException.notFound("Vault item not found"));
    }

    private VaultFile getFile(String userCode, String fileCode) {
        return vaultFileRepository.findByFileCodeAndUserCode(fileCode, userCode)
                .filter(file -> AppConstants.STATUS_ACTIVE.equalsIgnoreCase(file.getStatus()))
                .orElseThrow(() -> VaultException.notFound("File not found"));
    }

    private boolean requiresExtraProtection(VaultItem item) {
        return AppConstants.SECURITY_SECRET.equalsIgnoreCase(item.getSecurityLevel())
                || AppConstants.SECURITY_CRITICAL.equalsIgnoreCase(item.getSecurityLevel());
    }

    private String safeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "upload.bin";
        }
        return Path.of(filename).getFileName().toString();
    }

    private VaultFileResponse toResponse(VaultFile file) {
        return new VaultFileResponse(
                file.getFileCode(),
                file.getVaultItemCode(),
                file.getOriginalFilename(),
                file.getMimeType(),
                file.getFileSize(),
                file.getChecksum(),
                file.getEncrypted(),
                file.getStatus(),
                file.getCreateDate()
        );
    }
}
