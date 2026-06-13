package com.moon.vault_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "vault_files",
        indexes = {
                @Index(name = "idx_vault_files_item_status", columnList = "vault_item_code,status"),
                @Index(name = "idx_vault_files_user_status", columnList = "user_code,status"),
                @Index(name = "idx_vault_files_checksum", columnList = "checksum")
        }
)
public class VaultFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_code", length = 100, nullable = false, unique = true)
    private String fileCode;

    @Column(name = "vault_item_code", length = 100, nullable = false)
    private String vaultItemCode;

    @Column(name = "user_code", length = 100, nullable = false)
    private String userCode;

    @Column(name = "original_filename", length = 500, nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", length = 500, nullable = false)
    private String storedFilename;

    @Column(name = "file_path", length = 1000, nullable = false)
    private String filePath;

    @Column(name = "mime_type", length = 100, nullable = false)
    private String mimeType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "checksum", length = 255, nullable = false)
    private String checksum;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean encrypted = false;

    @Column(name = "encryption_iv", length = 255)
    private String encryptionIv;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
