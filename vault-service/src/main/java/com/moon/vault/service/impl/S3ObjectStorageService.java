package com.moon.vault.service.impl;

import com.moon.vault.common.ErrorCode;
import com.moon.vault.exception.VaultException;
import com.moon.vault.service.ObjectStorageService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "vault.storage.type", havingValue = "s3")
public class S3ObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;

    @Value("${vault.s3.bucket}")
    private String bucket;

    @Override
    public void put(String objectKey, byte[] content, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .contentType(contentType)
                            .stream(new ByteArrayInputStream(content), content.length, -1)
                            .build()
            );
        } catch (Exception exception) {
            throw new VaultException(ErrorCode.INTERNAL_ERROR, "Object storage upload failed");
        }
    }

    @Override
    public byte[] get(String objectKey) {
        try (var input = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        )) {
            return input.readAllBytes();
        } catch (Exception exception) {
            throw VaultException.notFound("File content not found");
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception exception) {
            throw new VaultException(ErrorCode.INTERNAL_ERROR, "Object storage delete failed");
        }
    }
}
