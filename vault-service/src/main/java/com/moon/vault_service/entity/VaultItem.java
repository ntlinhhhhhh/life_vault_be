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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "vault_items",
        indexes = {
                @Index(name = "idx_vault_items_user_folder", columnList = "user_code,folder_code"),
                @Index(name = "idx_vault_items_user_status", columnList = "user_code,status"),
                @Index(name = "idx_vault_items_user_category_type", columnList = "user_code,category,type"),
                @Index(name = "idx_vault_items_security_level", columnList = "security_level"),
                @Index(name = "idx_vault_items_expiration_date", columnList = "expiration_date")
        }
)
public class VaultItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vault_item_code", length = 100, nullable = false, unique = true)
    private String vaultItemCode;

    @Column(name = "user_code", length = 100, nullable = false)
    private String userCode;

    @Column(name = "folder_code", length = 100)
    private String folderCode;

    @Column(name = "title", length = 500, nullable = false)
    private String title;

    @Column(name = "type", length = 50, nullable = false)
    private String type;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "security_level", length = 30, nullable = false)
    private String securityLevel = "NORMAL";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "encrypted_content", columnDefinition = "TEXT")
    private String encryptedContent;

    @Column(name = "encryption_iv", length = 255)
    private String encryptionIv;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "status", length = 30, nullable = false)
    private String status = "ACTIVE";

    @Column(name = "is_favorite", nullable = false)
    private Boolean favorite = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
