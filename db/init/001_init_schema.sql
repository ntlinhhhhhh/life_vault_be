CREATE DATABASE IF NOT EXISTS life_vault_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE life_vault_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_code VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    failed_login_count INT NOT NULL DEFAULT 0,
    locked_until DATETIME(6) NULL,
    last_login_at DATETIME(6) NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_user_code (user_code),
    UNIQUE KEY uk_users_username (username),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    role_code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_code VARCHAR(100) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (user_code, role_code),
    KEY idx_user_roles_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    session_code VARCHAR(100) NOT NULL,
    user_code VARCHAR(100) NOT NULL,
    refresh_token_hash VARCHAR(255) NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    device_name VARCHAR(255) NULL,
    ip_address VARCHAR(100) NULL,
    user_agent TEXT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    expires_at DATETIME(6) NOT NULL,
    revoked_at DATETIME(6) NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_sessions_session_code (session_code),
    UNIQUE KEY uk_user_sessions_refresh_hash (refresh_token_hash),
    KEY idx_user_sessions_user_status (user_code, status),
    KEY idx_user_sessions_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    login_log_code VARCHAR(100) NOT NULL,
    user_code VARCHAR(100) NULL,
    username_input VARCHAR(255) NOT NULL,
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255) NULL,
    ip_address VARCHAR(100) NULL,
    user_agent TEXT NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_login_logs_login_log_code (login_log_code),
    KEY idx_login_logs_user_date (user_code, create_date),
    KEY idx_login_logs_success_date (success, create_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS folders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    folder_code VARCHAR(100) NOT NULL,
    user_code VARCHAR(100) NOT NULL,
    parent_folder_code VARCHAR(100) NULL,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(1000) NULL,
    sort_order INT NOT NULL DEFAULT 0,
    deleted_at DATETIME(6) NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_folders_folder_code (folder_code),
    KEY idx_folders_user_parent (user_code, parent_folder_code),
    KEY idx_folders_user_name (user_code, name),
    KEY idx_folders_workflow_state (workflow_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tag_code VARCHAR(100) NOT NULL,
    user_code VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(20) NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tags_tag_code (tag_code),
    UNIQUE KEY uk_tags_user_name (user_code, name),
    KEY idx_tags_user_code (user_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vault_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    vault_item_code VARCHAR(100) NOT NULL,
    user_code VARCHAR(100) NOT NULL,
    folder_code VARCHAR(100) NULL,
    title VARCHAR(500) NOT NULL,
    type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    security_level VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    description TEXT NULL,
    metadata_json TEXT NULL,
    encrypted_content TEXT NULL,
    encryption_iv VARCHAR(255) NULL,
    expiration_date DATE NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    is_favorite BOOLEAN NOT NULL DEFAULT FALSE,
    deleted_at DATETIME(6) NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vault_items_vault_item_code (vault_item_code),
    KEY idx_vault_items_user_folder (user_code, folder_code),
    KEY idx_vault_items_user_status (user_code, status),
    KEY idx_vault_items_user_category_type (user_code, category, type),
    KEY idx_vault_items_security_level (security_level),
    KEY idx_vault_items_expiration_date (expiration_date),
    FULLTEXT KEY ft_vault_items_search (title, description)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vault_item_tags (
    vault_item_code VARCHAR(100) NOT NULL,
    tag_code VARCHAR(100) NOT NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (vault_item_code, tag_code),
    KEY idx_vault_item_tags_tag_code (tag_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS vault_files (
    id BIGINT NOT NULL AUTO_INCREMENT,
    file_code VARCHAR(100) NOT NULL,
    vault_item_code VARCHAR(100) NOT NULL,
    user_code VARCHAR(100) NOT NULL,
    original_filename VARCHAR(500) NOT NULL,
    stored_filename VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    checksum VARCHAR(255) NOT NULL,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    encryption_iv VARCHAR(255) NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    deleted_at DATETIME(6) NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_vault_files_file_code (file_code),
    KEY idx_vault_files_item_status (vault_item_code, status),
    KEY idx_vault_files_user_status (user_code, status),
    KEY idx_vault_files_checksum (checksum)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS password_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    password_entry_code VARCHAR(100) NOT NULL,
    vault_item_code VARCHAR(100) NOT NULL,
    service_name VARCHAR(255) NOT NULL,
    login_url VARCHAR(1000) NULL,
    username_encrypted TEXT NULL,
    username_iv VARCHAR(255) NULL,
    password_encrypted TEXT NOT NULL,
    password_iv VARCHAR(255) NOT NULL,
    note_encrypted TEXT NULL,
    note_iv VARCHAR(255) NULL,
    last_changed_at DATETIME(6) NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_password_entries_code (password_entry_code),
    UNIQUE KEY uk_password_entries_item_code (vault_item_code),
    KEY idx_password_entries_service_name (service_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS reminders (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reminder_code VARCHAR(100) NOT NULL,
    vault_item_code VARCHAR(100) NOT NULL,
    user_code VARCHAR(100) NOT NULL,
    remind_at DATETIME(6) NOT NULL,
    reminder_type VARCHAR(50) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    sent_at DATETIME(6) NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reminders_reminder_code (reminder_code),
    KEY idx_reminders_user_status (user_code, status),
    KEY idx_reminders_item_status (vault_item_code, status),
    KEY idx_reminders_remind_at (remind_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    audit_log_code VARCHAR(100) NOT NULL,
    user_code VARCHAR(100) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    target_type VARCHAR(100) NOT NULL,
    target_code VARCHAR(100) NULL,
    ip_address VARCHAR(100) NULL,
    user_agent TEXT NULL,
    success BOOLEAN NOT NULL,
    failure_reason VARCHAR(255) NULL,
    create_user VARCHAR(100) NULL,
    create_date DATETIME(6) NULL,
    update_user VARCHAR(100) NULL,
    update_date DATETIME(6) NULL,
    workflow_state VARCHAR(100) NOT NULL DEFAULT 'active',
    sync_source VARCHAR(300) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_audit_logs_audit_log_code (audit_log_code),
    KEY idx_audit_logs_user_date (user_code, create_date),
    KEY idx_audit_logs_action_date (action_type, create_date),
    KEY idx_audit_logs_target (target_type, target_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO roles (role_code, name, create_user, create_date, workflow_state)
VALUES ('USER', 'Standard User', 'system', CURRENT_TIMESTAMP(6), 'active')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    update_user = 'system',
    update_date = CURRENT_TIMESTAMP(6),
    workflow_state = 'active';
