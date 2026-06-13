package com.moon.vault.entity;

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
        name = "password_entries",
        indexes = {
                @Index(name = "idx_password_entries_service_name", columnList = "service_name")
        }
)
public class PasswordEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "password_entry_code", length = 100, nullable = false, unique = true)
    private String passwordEntryCode;

    @Column(name = "vault_item_code", length = 100, nullable = false, unique = true)
    private String vaultItemCode;

    @Column(name = "service_name", length = 255, nullable = false)
    private String serviceName;

    @Column(name = "login_url", length = 1000)
    private String loginUrl;

    @Column(name = "username_encrypted", columnDefinition = "TEXT")
    private String usernameEncrypted;

    @Column(name = "username_iv", length = 255)
    private String usernameIv;

    @Column(name = "password_encrypted", columnDefinition = "TEXT", nullable = false)
    private String passwordEncrypted;

    @Column(name = "password_iv", length = 255, nullable = false)
    private String passwordIv;

    @Column(name = "note_encrypted", columnDefinition = "TEXT")
    private String noteEncrypted;

    @Column(name = "note_iv", length = 255)
    private String noteIv;

    @Column(name = "last_changed_at")
    private LocalDateTime lastChangedAt;
}
