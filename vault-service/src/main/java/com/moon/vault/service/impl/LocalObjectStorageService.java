package com.moon.vault.service.impl;

import com.moon.vault.common.ErrorCode;
import com.moon.vault.exception.VaultException;
import com.moon.vault.service.ObjectStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
@ConditionalOnProperty(name = "vault.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalObjectStorageService implements ObjectStorageService {

    @Value("${vault.storage.root:./storage/vault-files}")
    private String storageRoot;

    @Override
    public void put(String objectKey, byte[] content, String contentType) {
        try {
            Path target = resolve(objectKey);
            Files.createDirectories(target.getParent());
            Files.write(target, content);
        } catch (VaultException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new VaultException(ErrorCode.INTERNAL_ERROR, "File upload failed");
        }
    }

    @Override
    public byte[] get(String objectKey) {
        try {
            return Files.readAllBytes(resolve(objectKey));
        } catch (VaultException exception) {
            throw exception;
        } catch (Exception exception) {
            throw VaultException.notFound("File content not found");
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (VaultException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new VaultException(ErrorCode.INTERNAL_ERROR, "File delete failed");
        }
    }

    private Path resolve(String objectKey) {
        Path root = Path.of(storageRoot).toAbsolutePath().normalize();
        Path target = root.resolve(objectKey).normalize();
        if (!target.startsWith(root)) {
            throw VaultException.badRequest("Invalid file path");
        }
        return target;
    }
}
