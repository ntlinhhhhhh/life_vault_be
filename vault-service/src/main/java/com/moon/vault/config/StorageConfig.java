package com.moon.vault.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "vault.storage.type", havingValue = "s3")
    public MinioClient minioClient(
            @Value("${vault.s3.endpoint}") String endpoint,
            @Value("${vault.s3.access-key}") String accessKey,
            @Value("${vault.s3.secret-key}") String secretKey
    ) {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "vault.storage.type", havingValue = "s3")
    public ApplicationRunner minioBucketInitializer(
            MinioClient minioClient,
            @Value("${vault.s3.bucket}") String bucket
    ) {
        return args -> {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        };
    }
}
