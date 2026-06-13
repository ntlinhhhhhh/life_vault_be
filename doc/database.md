# Life Vault Database

## Tổng quan

Database: `life_vault_db`

Schema MVP có 13 bảng:

```text
users
roles
user_roles
user_sessions
login_logs
folders
tags
vault_items
vault_item_tags
vault_files
password_entries
reminders
audit_logs
```

Nguyên tắc thiết kế:

- Mỗi bảng có `id` BIGINT làm khóa chính nội bộ nếu bảng không dùng composite key.
- Quan hệ nghiệp vụ join bằng `*_code`, không join bằng `id`.
- Không tạo foreign key vật lý trong SQL init MVP.
- Các bảng dữ liệu theo user đều có `user_code`.
- Các trường nhạy cảm lưu dạng hash hoặc encrypted.

## Trường dùng chung

Tất cả bảng có các trường sau:

| Cột | Kiểu | Null | Default | Ghi chú |
|---|---|---:|---|---|
| `create_user` | VARCHAR(100) | Có | NULL | Người tạo bản ghi |
| `create_date` | DATETIME(6) | Có | NULL | Ngày tạo bản ghi |
| `update_user` | VARCHAR(100) | Có | NULL | Người cập nhật bản ghi |
| `update_date` | DATETIME(6) | Có | NULL | Ngày cập nhật bản ghi |
| `workflow_state` | VARCHAR(100) | Không | `active` | Trạng thái hiện tại |
| `sync_source` | VARCHAR(300) | Có | NULL | Nguồn đồng bộ |

## Quan hệ

| Quan hệ | Cardinality | Join bằng | Ghi chú |
|---|---|---|---|
| `users` -> `user_sessions` | 1-n | `users.user_code = user_sessions.user_code` | Một user có nhiều session |
| `users` -> `login_logs` | 1-n | `users.user_code = login_logs.user_code` | `login_logs.user_code` có thể NULL |
| `users` -> `folders` | 1-n | `users.user_code = folders.user_code` | Folder thuộc một user |
| `folders` -> `folders` | 1-n self | `folders.folder_code = folders.parent_folder_code` | Cây folder |
| `users` -> `tags` | 1-n | `users.user_code = tags.user_code` | Tag thuộc một user |
| `users` -> `vault_items` | 1-n | `users.user_code = vault_items.user_code` | Item thuộc một user |
| `folders` -> `vault_items` | 1-n | `folders.folder_code = vault_items.folder_code` | Item có thể nằm trong folder |
| `vault_items` -> `vault_files` | 1-n | `vault_items.vault_item_code = vault_files.vault_item_code` | Một item có nhiều file |
| `vault_items` -> `password_entries` | 1-1 | `vault_items.vault_item_code = password_entries.vault_item_code` | Chỉ item loại password có entry |
| `vault_items` -> `reminders` | 1-n | `vault_items.vault_item_code = reminders.vault_item_code` | Một item có nhiều reminder |
| `users` -> `audit_logs` | 1-n | `users.user_code = audit_logs.user_code` | Log theo người thực hiện |
| `vault_items` -> `tags` | n-n | Qua `vault_item_tags` | Bảng nối dùng `vault_item_code`, `tag_code` |
| `audit_logs` -> target | polymorphic | `target_type`, `target_code` | Target có thể là item, file, password |

## Bảng chi tiết

### `users`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `user_code` | VARCHAR(100) | Không | UK |  | Mã user dùng để join |
| `username` | VARCHAR(100) | Không | UK |  | Tên đăng nhập |
| `email` | VARCHAR(255) | Không | UK |  | Email |
| `password_hash` | VARCHAR(255) | Không |  |  | BCrypt hash |
| `status` | VARCHAR(30) | Không | IDX | `ACTIVE` | ACTIVE, LOCKED, DISABLED |
| `failed_login_count` | INT | Không |  | `0` | Số lần đăng nhập sai |
| `locked_until` | DATETIME(6) | Có |  | NULL | Khóa tạm |
| `last_login_at` | DATETIME(6) | Có |  | NULL | Lần đăng nhập cuối |

Index:

```sql
UNIQUE KEY uk_users_user_code (user_code)
UNIQUE KEY uk_users_username (username)
UNIQUE KEY uk_users_email (email)
KEY idx_users_status (status)
```

### `roles`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `role_code` | VARCHAR(50) | Không | UK |  | USER, ADMIN |
| `name` | VARCHAR(100) | Không |  |  | Tên hiển thị |

Index:

```sql
UNIQUE KEY uk_roles_role_code (role_code)
```

Seed:

```sql
role_code = 'USER'
name = 'Standard User'
```

### `user_roles`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `user_code` | VARCHAR(100) | Không | PK |  | Join tới `users.user_code` |
| `role_code` | VARCHAR(50) | Không | PK, IDX |  | Join tới `roles.role_code` |

Index:

```sql
PRIMARY KEY (user_code, role_code)
KEY idx_user_roles_role_code (role_code)
```

### `user_sessions`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `session_code` | VARCHAR(100) | Không | UK |  | Mã session |
| `user_code` | VARCHAR(100) | Không | IDX |  | Join tới `users.user_code` |
| `refresh_token_hash` | VARCHAR(255) | Không | UK |  | Hash refresh token |
| `device_id` | VARCHAR(255) | Không |  |  | ID thiết bị |
| `device_name` | VARCHAR(255) | Có |  | NULL | Tên thiết bị |
| `ip_address` | VARCHAR(100) | Có |  | NULL | IP |
| `user_agent` | TEXT | Có |  | NULL | User agent |
| `status` | VARCHAR(30) | Không | IDX | `ACTIVE` | ACTIVE, REVOKED, EXPIRED |
| `expires_at` | DATETIME(6) | Không | IDX |  | Hạn session |
| `revoked_at` | DATETIME(6) | Có |  | NULL | Thời điểm revoke |

Index:

```sql
UNIQUE KEY uk_user_sessions_session_code (session_code)
UNIQUE KEY uk_user_sessions_refresh_hash (refresh_token_hash)
KEY idx_user_sessions_user_status (user_code, status)
KEY idx_user_sessions_expires_at (expires_at)
```

### `login_logs`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `login_log_code` | VARCHAR(100) | Không | UK |  | Mã log |
| `user_code` | VARCHAR(100) | Có | IDX | NULL | Null nếu login sai username |
| `username_input` | VARCHAR(255) | Không |  |  | Username/email input |
| `success` | BOOLEAN | Không | IDX |  | Thành công/thất bại |
| `failure_reason` | VARCHAR(255) | Có |  | NULL | Lý do lỗi |
| `ip_address` | VARCHAR(100) | Có |  | NULL | IP |
| `user_agent` | TEXT | Có |  | NULL | User agent |

Index:

```sql
UNIQUE KEY uk_login_logs_login_log_code (login_log_code)
KEY idx_login_logs_user_date (user_code, create_date)
KEY idx_login_logs_success_date (success, create_date)
```

### `folders`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `folder_code` | VARCHAR(100) | Không | UK |  | Mã folder |
| `user_code` | VARCHAR(100) | Không | IDX |  | Chủ sở hữu |
| `parent_folder_code` | VARCHAR(100) | Có | IDX | NULL | Folder cha |
| `name` | VARCHAR(255) | Không | IDX |  | Tên folder |
| `path` | VARCHAR(1000) | Có |  | NULL | Đường dẫn cache |
| `sort_order` | INT | Không |  | `0` | Thứ tự |
| `deleted_at` | DATETIME(6) | Có |  | NULL | Xóa mềm |

Index:

```sql
UNIQUE KEY uk_folders_folder_code (folder_code)
KEY idx_folders_user_parent (user_code, parent_folder_code)
KEY idx_folders_user_name (user_code, name)
KEY idx_folders_workflow_state (workflow_state)
```

### `tags`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `tag_code` | VARCHAR(100) | Không | UK |  | Mã tag |
| `user_code` | VARCHAR(100) | Không | UK, IDX |  | Chủ sở hữu |
| `name` | VARCHAR(100) | Không | UK |  | Tên tag |
| `color` | VARCHAR(20) | Có |  | NULL | Màu |

Index:

```sql
UNIQUE KEY uk_tags_tag_code (tag_code)
UNIQUE KEY uk_tags_user_name (user_code, name)
KEY idx_tags_user_code (user_code)
```

### `vault_items`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `vault_item_code` | VARCHAR(100) | Không | UK |  | Mã vault item |
| `user_code` | VARCHAR(100) | Không | IDX |  | Chủ sở hữu |
| `folder_code` | VARCHAR(100) | Có | IDX | NULL | Join tới folder |
| `title` | VARCHAR(500) | Không | FULLTEXT |  | Tên item |
| `type` | VARCHAR(50) | Không | IDX |  | Loại item |
| `category` | VARCHAR(50) | Không | IDX |  | Nhóm dữ liệu |
| `security_level` | VARCHAR(30) | Không | IDX | `NORMAL` | NORMAL, SECRET, CRITICAL |
| `description` | TEXT | Có | FULLTEXT | NULL | Mô tả |
| `metadata_json` | TEXT | Có |  | NULL | Metadata không nhạy cảm |
| `encrypted_content` | TEXT | Có |  | NULL | Nội dung nhạy cảm mã hóa |
| `encryption_iv` | VARCHAR(255) | Có |  | NULL | IV |
| `expiration_date` | DATE | Có | IDX | NULL | Ngày hết hạn |
| `status` | VARCHAR(30) | Không | IDX | `ACTIVE` | ACTIVE, DELETED |
| `is_favorite` | BOOLEAN | Không |  | `FALSE` | Yêu thích |
| `deleted_at` | DATETIME(6) | Có |  | NULL | Xóa mềm |

Index:

```sql
UNIQUE KEY uk_vault_items_vault_item_code (vault_item_code)
KEY idx_vault_items_user_folder (user_code, folder_code)
KEY idx_vault_items_user_status (user_code, status)
KEY idx_vault_items_user_category_type (user_code, category, type)
KEY idx_vault_items_security_level (security_level)
KEY idx_vault_items_expiration_date (expiration_date)
FULLTEXT KEY ft_vault_items_search (title, description)
```

### `vault_item_tags`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `vault_item_code` | VARCHAR(100) | Không | PK |  | Join tới `vault_items.vault_item_code` |
| `tag_code` | VARCHAR(100) | Không | PK, IDX |  | Join tới `tags.tag_code` |

Index:

```sql
PRIMARY KEY (vault_item_code, tag_code)
KEY idx_vault_item_tags_tag_code (tag_code)
```

### `vault_files`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `file_code` | VARCHAR(100) | Không | UK |  | Mã file |
| `vault_item_code` | VARCHAR(100) | Không | IDX |  | Join tới vault item |
| `user_code` | VARCHAR(100) | Không | IDX |  | Chủ sở hữu |
| `original_filename` | VARCHAR(500) | Không |  |  | Tên gốc |
| `stored_filename` | VARCHAR(500) | Không |  |  | Tên lưu |
| `file_path` | VARCHAR(1000) | Không |  |  | Đường dẫn lưu |
| `mime_type` | VARCHAR(100) | Không |  |  | MIME type |
| `file_size` | BIGINT | Không |  |  | Dung lượng |
| `checksum` | VARCHAR(255) | Không | IDX |  | SHA-256 |
| `is_encrypted` | BOOLEAN | Không |  | `FALSE` | File đã mã hóa |
| `encryption_iv` | VARCHAR(255) | Có |  | NULL | IV file |
| `status` | VARCHAR(30) | Không | IDX | `ACTIVE` | ACTIVE, DELETED |
| `deleted_at` | DATETIME(6) | Có |  | NULL | Xóa mềm |

Index:

```sql
UNIQUE KEY uk_vault_files_file_code (file_code)
KEY idx_vault_files_item_status (vault_item_code, status)
KEY idx_vault_files_user_status (user_code, status)
KEY idx_vault_files_checksum (checksum)
```

### `password_entries`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `password_entry_code` | VARCHAR(100) | Không | UK |  | Mã password entry |
| `vault_item_code` | VARCHAR(100) | Không | UK |  | Join tới vault item |
| `service_name` | VARCHAR(255) | Không | IDX |  | Tên dịch vụ |
| `login_url` | VARCHAR(1000) | Có |  | NULL | URL đăng nhập |
| `username_encrypted` | TEXT | Có |  | NULL | Username mã hóa |
| `username_iv` | VARCHAR(255) | Có |  | NULL | IV username |
| `password_encrypted` | TEXT | Không |  |  | Password mã hóa |
| `password_iv` | VARCHAR(255) | Không |  |  | IV password |
| `note_encrypted` | TEXT | Có |  | NULL | Note mã hóa |
| `note_iv` | VARCHAR(255) | Có |  | NULL | IV note |
| `last_changed_at` | DATETIME(6) | Có |  | NULL | Lần đổi mật khẩu |

Index:

```sql
UNIQUE KEY uk_password_entries_code (password_entry_code)
UNIQUE KEY uk_password_entries_item_code (vault_item_code)
KEY idx_password_entries_service_name (service_name)
```

### `reminders`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `reminder_code` | VARCHAR(100) | Không | UK |  | Mã reminder |
| `vault_item_code` | VARCHAR(100) | Không | IDX |  | Join tới vault item |
| `user_code` | VARCHAR(100) | Không | IDX |  | Chủ sở hữu |
| `remind_at` | DATETIME(6) | Không | IDX |  | Thời điểm nhắc |
| `reminder_type` | VARCHAR(50) | Không |  |  | EXPIRE_30_DAYS, EXPIRE_7_DAYS |
| `status` | VARCHAR(30) | Không | IDX | `PENDING` | PENDING, SENT, READ, DISMISSED |
| `sent_at` | DATETIME(6) | Có |  | NULL | Thời điểm gửi |

Index:

```sql
UNIQUE KEY uk_reminders_reminder_code (reminder_code)
KEY idx_reminders_user_status (user_code, status)
KEY idx_reminders_item_status (vault_item_code, status)
KEY idx_reminders_remind_at (remind_at)
```

### `audit_logs`

| Cột | Kiểu | Null | Key | Default | Ghi chú |
|---|---|---:|---|---|---|
| `id` | BIGINT | Không | PK | AUTO_INCREMENT | Khóa chính nội bộ |
| `audit_log_code` | VARCHAR(100) | Không | UK |  | Mã audit log |
| `user_code` | VARCHAR(100) | Không | IDX |  | Người thực hiện |
| `action_type` | VARCHAR(100) | Không | IDX |  | Loại hành động |
| `target_type` | VARCHAR(100) | Không | IDX |  | Loại target |
| `target_code` | VARCHAR(100) | Có | IDX | NULL | Mã target |
| `ip_address` | VARCHAR(100) | Có |  | NULL | IP |
| `user_agent` | TEXT | Có |  | NULL | User agent |
| `success` | BOOLEAN | Không |  |  | Thành công/thất bại |
| `failure_reason` | VARCHAR(255) | Có |  | NULL | Lý do lỗi |

Index:

```sql
UNIQUE KEY uk_audit_logs_audit_log_code (audit_log_code)
KEY idx_audit_logs_user_date (user_code, create_date)
KEY idx_audit_logs_action_date (action_type, create_date)
KEY idx_audit_logs_target (target_type, target_code)
```
