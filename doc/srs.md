# PERSONAL LIFE VAULT MOBILE APP

## SRS + Thiết kế kiến trúc + Database + API + Kế hoạch triển khai

| Thuộc tính | Giá trị |
|---|---|
| Tên hệ thống | Personal Life Vault |
| Loại hệ thống | Mobile app lưu trữ thông tin cá nhân bảo mật cao |
| Client chính | Mobile App |
| Backend | Java Spring Boot |
| Database | 1 SQL database dùng chung cho toàn hệ thống |
| Cache/Security | Redis |
| Kiến trúc MVP | api-gateway, auth-service, vault-service |
| Service chính | vault-service chứa toàn bộ nghiệp vụ chính |
| Mức ưu tiên bảo mật | Rất cao |
| Phiên bản tài liệu | 1.0 - Mobile First MVP Architecture |
| Trạng thái | Bản đặc tả phục vụ thiết kế và triển khai |

---

# MỤC LỤC

1. Giới thiệu  
2. Phạm vi hệ thống  
3. Actor và quyền truy cập  
4. Kiến trúc tổng thể  
5. Kiến trúc backend MVP  
6. Cấu trúc repository  
7. Chi tiết từng service  
8. Mô hình bảo mật  
9. Phân loại dữ liệu, tag và security level  
10. Yêu cầu chức năng  
11. Use case chi tiết  
12. Luồng trạng thái  
13. Thiết kế database  
14. Phân tích từng bảng  
15. API endpoint theo từng module  
16. Luồng nghiệp vụ chính  
17. Cấu trúc source code chi tiết  
18. Kế hoạch triển khai tuần tự  
19. Roadmap MVP theo tuần  
20. Tiêu chí nghiệm thu  
21. Open issues  
22. Kết luận  

---

# 1. GIỚI THIỆU

## 1.1 Bối cảnh

Người dùng cá nhân thường lưu nhiều thông tin quan trọng ở nhiều nơi khác nhau như ảnh điện thoại, Google Drive, Zalo, Messenger, Telegram Saved Messages hoặc ghi chú cá nhân. Các thông tin này có thể bao gồm:

- Mật khẩu tài khoản cá nhân.
- Căn cước công dân.
- Bảo hiểm y tế.
- Bằng lái xe.
- Hộ chiếu.
- Bằng cấp.
- Chứng chỉ.
- CV.
- Mã sinh viên.
- Mã số thuế.
- Thông tin tài khoản ngân hàng.
- Các file ảnh/PDF quan trọng.
- Ghi chú nhạy cảm cần dùng lại.

Cách lưu rời rạc hiện tại có nhiều vấn đề:

- Khó tìm lại khi cần gấp.
- Không có phân loại rõ ràng.
- Không có tag, folder hoặc tìm kiếm nhanh.
- Không có nhắc nhở giấy tờ sắp hết hạn.
- Không biết lịch sử xem/sửa/tải dữ liệu.
- Mật khẩu hoặc số giấy tờ có thể bị lưu plaintext.
- Dữ liệu dễ bị lộ nếu điện thoại hoặc tài khoản cloud bị truy cập trái phép.

Personal Life Vault được xây dựng để giải quyết bài toán này bằng một mobile app có backend Java và SQL database, giúp lưu trữ dữ liệu cá nhân một cách tập trung, có mã hóa, có phân loại, có tìm kiếm và có audit log.

---

## 1.2 Mục tiêu hệ thống

Hệ thống cần đạt các mục tiêu sau:

1. Cho phép người dùng lưu trữ tài liệu cá nhân quan trọng.
2. Cho phép lưu mật khẩu dưới dạng mã hóa.
3. Cho phép upload ảnh/PDF giấy tờ.
4. Cho phép phân loại theo folder, category, tag và security level.
5. Cho phép tìm kiếm nhanh trên mobile.
6. Cho phép nhắc giấy tờ sắp hết hạn.
7. Cho phép ghi log các hành động nhạy cảm.
8. Đảm bảo user chỉ truy cập được dữ liệu của chính mình.
9. Đảm bảo dữ liệu nhạy cảm không lưu plaintext trong database.
10. Kiến trúc đủ đơn giản để triển khai MVP nhưng vẫn có khả năng mở rộng.

---

## 1.3 Định hướng kiến trúc

Vì đây là app cá nhân, không nên bắt đầu bằng kiến trúc microservice quá lớn. Kiến trúc MVP được chọn là:

```text
Mobile App
    |
    v
API Gateway
    |
    +----------------+
    |                |
    v                v
Auth Service     Vault Service
    |                |
    +--------+-------+
             |
             v
        1 SQL Database
             |
             v
           Redis
```

Trong đó:

- `api-gateway` là cửa vào duy nhất cho mobile app.
- `auth-service` xử lý xác thực, token, session, user.
- `vault-service` chứa toàn bộ nghiệp vụ chính: folder, tag, vault item, file, password vault, reminder, audit log, search, encryption.
- Chỉ dùng 1 SQL database cho toàn bộ bảng để giảm độ phức tạp triển khai.
- Redis dùng cho blacklist token, session cache hoặc rate limit.

---

# 2. PHẠM VI HỆ THỐNG

## 2.1 Trong phạm vi MVP

| Nhóm chức năng | Mô tả |
|---|---|
| Authentication | Đăng ký, đăng nhập, refresh token, logout, đổi mật khẩu |
| API Gateway | Route request, kiểm tra JWT, kiểm tra token blacklist |
| User Session | Quản lý refresh token và thiết bị đăng nhập |
| Vault PIN/Re-auth | Yêu cầu xác thực lại khi xem dữ liệu CRITICAL |
| Folder | Tạo, sửa, xóa, lấy cây folder |
| Tag | Tạo, sửa, xóa, gán tag cho item |
| Vault Item | Tạo, sửa, xem, xóa mềm, khôi phục, archive, favorite |
| Password Vault | Lưu username/password/note đã mã hóa |
| File Storage | Upload/download ảnh hoặc PDF giấy tờ |
| Search | Tìm kiếm theo keyword, tag, folder, category, type |
| Reminder | Nhắc giấy tờ sắp hết hạn |
| Audit Log | Ghi log xem, tải, sửa, xóa, reveal password |
| Encryption | Mã hóa trường nhạy cảm và file nhạy cảm |
| Mobile API | REST API tối ưu cho mobile app |

---

## 2.2 Ngoài phạm vi MVP

| Chức năng | Lý do hoãn |
|---|---|
| OCR đọc CCCD/BHYT | Cần xử lý ảnh, chưa cần cho MVP |
| AI phân loại tài liệu | Chưa cần ở giai đoạn đầu |
| Chia sẻ tài liệu bằng link | Rủi ro bảo mật cao |
| Multi-user/family vault | App cá nhân, chưa cần chia sẻ |
| Web admin đầy đủ | Mobile app là client chính |
| MinIO/S3 | Ban đầu dùng local storage |
| Push notification | Có thể thêm giai đoạn 2 |
| Search engine Elasticsearch | Database search là đủ cho MVP |
| End-to-end encryption hoàn chỉnh | Phức tạp, nghiên cứu sau |
| Backup cloud | Giai đoạn sau |

---

# 3. ACTOR VÀ QUYỀN TRUY CẬP

## 3.1 Actor

| Actor | Mô tả | Quyền |
|---|---|---|
| Mobile User | Người dùng cá nhân sử dụng app | Toàn quyền với dữ liệu của mình |
| API Gateway | Thành phần trung gian nhận request | Kiểm tra token và route request |
| Auth Service | Service xác thực | Quản lý tài khoản, token, session |
| Vault Service | Service nghiệp vụ chính | Quản lý dữ liệu vault |
| System Scheduler | Tác vụ chạy định kỳ | Tạo reminder, cập nhật trạng thái hết hạn |
| Backend Admin kỹ thuật | Người vận hành hệ thống | Không được xem plaintext dữ liệu nhạy cảm |

---

## 3.2 Ma trận quyền

| Chức năng | Mobile User | Gateway | Auth Service | Vault Service | Scheduler | Admin kỹ thuật |
|---|---:|---:|---:|---:|---:|---:|
| Đăng ký/đăng nhập | Có | Route | Xử lý | Không | Không | Không |
| Refresh token | Có | Route | Xử lý | Không | Không | Không |
| Logout | Có | Route | Xử lý blacklist | Không | Không | Không |
| Tạo vault item | Có | Route/Check JWT | Không | Xử lý | Không | Không |
| Xem vault item | Có, owner only | Route/Check JWT | Không | Xử lý owner check | Không | Không |
| Upload file | Có | Route/Check JWT | Không | Xử lý | Không | Không |
| Reveal password | Có, re-auth | Route/Check JWT | Cấp re-auth token | Giải mã | Không | Không |
| Tạo reminder | Có/System | Route | Không | Xử lý | Có | Không |
| Xem audit log | Có dữ liệu của mình | Route | Không | Xử lý | Không | Không |
| Health check | Không | Có | Có | Có | Không | Có |

---

# 4. KIẾN TRÚC TỔNG THỂ

## 4.1 Sơ đồ tổng thể

```text
+----------------------------+
|        Mobile App          |
| Flutter / Android Kotlin   |
+-------------+--------------+
              |
              | HTTPS REST API
              v
+----------------------------+
|        API Gateway         |
| Spring Cloud Gateway       |
| JWT Filter                 |
| Redis Blacklist Check      |
+-------------+--------------+
              |
      +-------+-------+
      |               |
      v               v
+-------------+   +-------------+
| Auth Service|   | Vault Service|
| Spring Boot |   | Spring Boot |
+------+------+   +------+------+
       |                 |
       +--------+--------+
                |
                v
+----------------------------+
|       Personal Vault DB    |
| MariaDB                    |
+----------------------------+
                |
                v
+----------------------------+
|           Redis            |
| blacklist/session/cache    |
+----------------------------+

+----------------------------+
| Local File Storage         |
| encrypted files            |
+----------------------------+
```

---

## 4.2 Lý do chọn 1 database

Vì đây là app cá nhân và MVP nhỏ, dùng 1 database có lợi hơn:

- Dễ thiết kế.
- Dễ debug.
- Dễ query.
- Dễ backup.
- Không cần xử lý distributed transaction.
- Không cần đồng bộ dữ liệu giữa nhiều service.
- Phù hợp với 3 module backend nhỏ.

Tên database đề xuất:

```text
life_vault_db
```

Các bảng trong database được chia theo nhóm logic:

```text
Auth tables:
- users
- roles
- user_roles
- user_sessions
- login_logs

Vault tables:
- folders
- tags
- vault_items
- vault_item_tags
- vault_files
- password_entries
- reminders
- audit_logs
```

---

# 5. KIẾN TRÚC BACKEND MVP

## 5.1 Danh sách service

| Service | Vai trò | Có DB riêng không? |
|---|---|---|
| api-gateway | Cửa vào hệ thống, route, validate JWT, check blacklist | Không |
| auth-service | Xác thực, user, token, session, login log | Dùng chung DB |
| vault-service | Toàn bộ nghiệp vụ vault chính | Dùng chung DB |

---

## 5.2 Nguyên tắc giao tiếp

- Mobile app chỉ gọi API Gateway.
- Gateway route request tới auth-service hoặc vault-service.
- Auth-service không gọi vault-service trong MVP.
- Vault-service nhận userCode từ JWT hoặc header nội bộ do gateway chuyển tiếp.
- Tất cả service cùng dùng chung JWT secret hoặc public key để validate token.
- Redis dùng chung cho blacklist token.

---

## 5.3 Port đề xuất

| Thành phần | Port |
|---|---:|
| api-gateway | 9000 |
| auth-service | 5001 |
| vault-service | 5002 |
| MariaDB | 3306 |
| Redis | 6379 |

---

# 6. CẤU TRÚC REPOSITORY

```text
personal-life-vault
├── pom.xml
├── README.md
├── .gitignore
├── .env
├── .env.example
├── docker-compose.yml
├── docs
│   ├── srs.md
│   ├── erd.md
│   ├── api.md
│   └── architecture.md
├── api-gateway
│   ├── pom.xml
│   └── src
├── auth-service
│   ├── pom.xml
│   └── src
├── vault-service
│   ├── pom.xml
│   └── src
└── common-lib
    ├── pom.xml
    └── src
```

---

## 6.1 File root cần có

| File | Mục đích |
|---|---|
| `pom.xml` | Parent Maven POM quản lý module |
| `.gitignore` | Bỏ qua target, .env, IDE files |
| `.env` | Biến môi trường local, không commit |
| `.env.example` | Mẫu biến môi trường, được commit |
| `docker-compose.yml` | Chạy MariaDB, Redis |
| `README.md` | Mô tả project |
| `docs/srs.md` | Tài liệu SRS này |
| `docs/erd.md` | Thiết kế ERD |
| `docs/api.md` | Tài liệu API |
| `docs/architecture.md` | Kiến trúc hệ thống |

---

# 7. CHI TIẾT TỪNG SERVICE

# 7.1 API Gateway

## 7.1.1 Vai trò

API Gateway không chứa nghiệp vụ chính. Gateway chỉ làm nhiệm vụ:

- Nhận toàn bộ request từ mobile app.
- Route `/api/auth/**` sang auth-service.
- Route `/api/vault/**` sang vault-service.
- Kiểm tra JWT với các API cần xác thực.
- Kiểm tra token đã logout trong Redis blacklist.
- Cấu hình CORS.
- Cấu hình rate limit cơ bản.
- Chuẩn hóa lỗi gateway.
- Health check.

---

## 7.1.2 Module trong api-gateway

| Module/package | Mục đích |
|---|---|
| `config` | Cấu hình route, security, CORS, Redis |
| `filter` | Global filter kiểm tra JWT/blacklist |
| `service` | JwtService, TokenBlacklistService |
| `exception` | Xử lý lỗi gateway |
| `constants` | Danh sách public endpoint, header constants |

---

## 7.1.3 File cần có

```text
api-gateway
└── src/main/java/com/plv/gateway
    ├── ApiGatewayApplication.java
    ├── config
    │   ├── GatewayRoutesConfig.java
    │   ├── SecurityConfig.java
    │   ├── CorsConfig.java
    │   └── RedisConfig.java
    ├── filter
    │   ├── JwtGlobalFilter.java
    │   └── RateLimitFilter.java
    ├── service
    │   ├── JwtService.java
    │   └── TokenBlacklistService.java
    ├── exception
    │   └── GatewayExceptionHandler.java
    └── constants
        └── GatewayConstants.java
```

---

## 7.1.4 Chức năng từng file

| File | Làm gì |
|---|---|
| `ApiGatewayApplication.java` | Class main chạy gateway |
| `GatewayRoutesConfig.java` | Định nghĩa route tới auth-service và vault-service |
| `SecurityConfig.java` | Cấu hình endpoint public/private |
| `CorsConfig.java` | Cho phép mobile app gọi API |
| `RedisConfig.java` | Kết nối Redis |
| `JwtGlobalFilter.java` | Đọc Authorization header, validate JWT |
| `RateLimitFilter.java` | Giới hạn request nếu cần |
| `JwtService.java` | Parse, validate JWT |
| `TokenBlacklistService.java` | Kiểm tra token có bị blacklist không |
| `GatewayExceptionHandler.java` | Trả lỗi 401/403/500 chuẩn |
| `GatewayConstants.java` | Hằng số route/header |

---

# 7.2 Auth Service

## 7.2.1 Vai trò

Auth-service chịu trách nhiệm:

- Đăng ký tài khoản.
- Đăng nhập.
- Sinh access token.
- Sinh refresh token.
- Refresh access token.
- Logout và revoke session.
- Đổi mật khẩu.
- Lấy thông tin user hiện tại.
- Quản lý user session.
- Ghi login log.
- Cấp re-auth token ngắn hạn để xem dữ liệu nhạy cảm.

Auth-service không xử lý:

- Folder.
- Tag.
- Vault item.
- File.
- Password vault.
- Reminder.
- Audit log vault.

---

## 7.2.2 Module trong auth-service

| Module/package | Mục đích |
|---|---|
| `config` | Security, Redis, password encoder |
| `controller` | REST API auth/user/session |
| `dto.request` | Request DTO |
| `dto.response` | Response DTO |
| `entity` | User, Role, Session, LoginLog |
| `repository` | JPA repository |
| `service` | Interface nghiệp vụ |
| `service.impl` | Triển khai nghiệp vụ |
| `security` | JWT, UserDetails, filter |
| `exception` | Xử lý lỗi auth |
| `mapper` | Convert Entity/DTO |
| `aop` | Ghi log nếu cần |

---

## 7.2.3 File cần có

```text
auth-service
└── src/main/java/com/plv/auth
    ├── AuthServiceApplication.java
    ├── config
    │   ├── SecurityConfig.java
    │   ├── RedisConfig.java
    │   └── PasswordConfig.java
    ├── controller
    │   ├── AuthController.java
    │   ├── UserController.java
    │   └── SessionController.java
    ├── dto
    │   ├── request
    │   │   ├── RegisterRequest.java
    │   │   ├── LoginRequest.java
    │   │   ├── RefreshTokenRequest.java
    │   │   ├── LogoutRequest.java
    │   │   ├── ChangePasswordRequest.java
    │   │   └── ReAuthRequest.java
    │   └── response
    │       ├── LoginResponse.java
    │       ├── UserInfoResponse.java
    │       ├── TokenResponse.java
    │       └── SessionResponse.java
    ├── entity
    │   ├── User.java
    │   ├── Role.java
    │   ├── UserRole.java
    │   ├── UserSession.java
    │   └── LoginLog.java
    ├── repository
    │   ├── UserRepository.java
    │   ├── RoleRepository.java
    │   ├── UserRoleRepository.java
    │   ├── UserSessionRepository.java
    │   └── LoginLogRepository.java
    ├── service
    │   ├── AuthService.java
    │   ├── UserService.java
    │   ├── SessionService.java
    │   ├── ReAuthService.java
    │   └── impl
    │       ├── AuthServiceImpl.java
    │       ├── UserServiceImpl.java
    │       ├── SessionServiceImpl.java
    │       └── ReAuthServiceImpl.java
    ├── security
    │   ├── JwtService.java
    │   ├── CustomUserDetailsService.java
    │   └── SecurityUser.java
    ├── mapper
    │   └── UserMapper.java
    └── exception
        └── AuthExceptionHandler.java
```

---

## 7.2.4 Chức năng từng controller

| Controller | Endpoint chính | Làm gì |
|---|---|---|
| `AuthController` | `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/change-password`, `/auth/re-auth` | Xử lý xác thực |
| `UserController` | `/auth/me`, `/auth/profile` | Lấy/cập nhật profile |
| `SessionController` | `/auth/sessions`, `/auth/sessions/{sessionCode}/revoke` | Quản lý thiết bị đăng nhập |

---

# 7.3 Vault Service

## 7.3.1 Vai trò

Vault-service là service nghiệp vụ chính của hệ thống. Service này chứa toàn bộ chức năng sau:

- Folder.
- Tag.
- Vault item.
- Password vault.
- File upload/download.
- Search/filter.
- Reminder.
- Audit log.
- Encryption.
- Masking dữ liệu.
- Owner check.

---

## 7.3.2 Module trong vault-service

| Module/package | Mục đích |
|---|---|
| `folder` hoặc `controller/service/entity` theo domain | Quản lý folder |
| `tag` | Quản lý tag |
| `item` | Quản lý vault item |
| `password` | Quản lý password entry |
| `file` | Upload/download file |
| `search` | Tìm kiếm |
| `reminder` | Nhắc hết hạn |
| `audit` | Audit log |
| `encryption` | Mã hóa/giải mã |
| `security` | Owner check, current user |
| `scheduler` | Tác vụ định kỳ |
| `aop` | Tự động ghi audit log |
| `mapper` | Entity/DTO |
| `exception` | Xử lý lỗi |

---

## 7.3.3 File cần có

```text
vault-service
└── src/main/java/com/plv/vault
    ├── VaultServiceApplication.java
    ├── config
    │   ├── VaultSecurityConfig.java
    │   ├── StorageConfig.java
    │   ├── EncryptionConfig.java
    │   └── SchedulerConfig.java
    ├── controller
    │   ├── FolderController.java
    │   ├── TagController.java
    │   ├── VaultItemController.java
    │   ├── PasswordController.java
    │   ├── FileController.java
    │   ├── SearchController.java
    │   ├── ReminderController.java
    │   └── AuditLogController.java
    ├── dto
    │   ├── request
    │   │   ├── CreateFolderRequest.java
    │   │   ├── UpdateFolderRequest.java
    │   │   ├── CreateTagRequest.java
    │   │   ├── CreateVaultItemRequest.java
    │   │   ├── UpdateVaultItemRequest.java
    │   │   ├── CreatePasswordRequest.java
    │   │   ├── RevealPasswordRequest.java
    │   │   └── SearchVaultItemRequest.java
    │   └── response
    │       ├── FolderResponse.java
    │       ├── TagResponse.java
    │       ├── VaultItemListResponse.java
    │       ├── VaultItemDetailResponse.java
    │       ├── PasswordDetailResponse.java
    │       ├── VaultFileResponse.java
    │       ├── ReminderResponse.java
    │       └── AuditLogResponse.java
    ├── entity
    │   ├── Folder.java
    │   ├── Tag.java
    │   ├── VaultItem.java
    │   ├── VaultItemTag.java
    │   ├── VaultFile.java
    │   ├── PasswordEntry.java
    │   ├── Reminder.java
    │   └── AuditLog.java
    ├── repository
    │   ├── FolderRepository.java
    │   ├── TagRepository.java
    │   ├── VaultItemRepository.java
    │   ├── VaultItemTagRepository.java
    │   ├── VaultFileRepository.java
    │   ├── PasswordEntryRepository.java
    │   ├── ReminderRepository.java
    │   └── AuditLogRepository.java
    ├── service
    │   ├── FolderService.java
    │   ├── TagService.java
    │   ├── VaultItemService.java
    │   ├── PasswordVaultService.java
    │   ├── FileStorageService.java
    │   ├── SearchService.java
    │   ├── ReminderService.java
    │   ├── AuditLogService.java
    │   ├── EncryptionService.java
    │   └── OwnerCheckService.java
    ├── service
    │   └── impl
    │       ├── FolderServiceImpl.java
    │       ├── TagServiceImpl.java
    │       ├── VaultItemServiceImpl.java
    │       ├── PasswordVaultServiceImpl.java
    │       ├── FileStorageServiceImpl.java
    │       ├── SearchServiceImpl.java
    │       ├── ReminderServiceImpl.java
    │       ├── AuditLogServiceImpl.java
    │       ├── EncryptionServiceImpl.java
    │       └── OwnerCheckServiceImpl.java
    ├── mapper
    │   ├── FolderMapper.java
    │   ├── TagMapper.java
    │   ├── VaultItemMapper.java
    │   ├── VaultFileMapper.java
    │   └── AuditLogMapper.java
    ├── scheduler
    │   └── ReminderScheduler.java
    ├── aop
    │   ├── AuditLogAspect.java
    │   └── UserActionLog.java
    ├── util
    │   ├── MaskingUtil.java
    │   ├── FileNameUtil.java
    │   └── ChecksumUtil.java
    └── exception
        └── VaultExceptionHandler.java
```

---

## 7.3.4 Chức năng từng controller trong vault-service

| Controller | Làm gì |
|---|---|
| `FolderController` | CRUD folder, lấy cây folder |
| `TagController` | CRUD tag, gán tag |
| `VaultItemController` | CRUD item, favorite, archive, restore, purge |
| `PasswordController` | Tạo password entry, reveal/copy password |
| `FileController` | Upload/download/delete file |
| `SearchController` | Tìm kiếm và filter item |
| `ReminderController` | Lấy reminder, đánh dấu đọc, gia hạn giấy tờ |
| `AuditLogController` | Xem lịch sử hoạt động |

---

# 7.4 Common Lib

## 7.4.1 Vai trò

`common-lib` chứa các thành phần dùng chung cho cả 3 service:

- ApiResponse.
- PageResponse.
- ErrorCode.
- BusinessException.
- Constants.
- Enum dùng chung.
- Date util.
- Masking util đơn giản.
- Base DTO.

---

## 7.4.2 File cần có

```text
common-lib
└── src/main/java/com/plv/common
    ├── response
    │   ├── ApiResponse.java
    │   └── PageResponse.java
    ├── exception
    │   ├── BusinessException.java
    │   ├── ErrorCode.java
    │   └── GlobalErrorResponse.java
    ├── constants
    │   ├── SecurityConstants.java
    │   └── AppConstants.java
    ├── enums
    │   ├── UserStatus.java
    │   ├── VaultItemType.java
    │   ├── VaultCategory.java
    │   ├── SecurityLevel.java
    │   ├── VaultItemStatus.java
    │   ├── ReminderStatus.java
    │   └── AuditActionType.java
    └── util
        ├── DateTimeUtil.java
        └── StringMaskingUtil.java
```

---

# 8. MÔ HÌNH BẢO MẬT

## 8.1 Nguyên tắc bảo mật

1. Tất cả API vault phải yêu cầu JWT.
2. User chỉ được truy cập dữ liệu có `user_code` của mình.
3. Mật khẩu đăng nhập phải hash bằng BCrypt.
4. Refresh token không lưu plaintext; chỉ lưu hash.
5. Access token sau logout phải đưa vào Redis blacklist.
6. Dữ liệu nhạy cảm phải mã hóa trước khi lưu database.
7. File SECRET/CRITICAL phải mã hóa trước khi lưu storage.
8. API list không được trả plaintext dữ liệu nhạy cảm.
9. Xem password phải yêu cầu re-auth token.
10. Download file nhạy cảm phải ghi audit log.
11. Không log password, token, số CCCD đầy đủ.
12. CORS chỉ cho phép origin/app hợp lệ.
13. Rate limit login để chống brute force.

---

## 8.2 Vault PIN và Re-auth

Mobile app nên có hai lớp bảo vệ:

### Lớp 1: Login

User đăng nhập bằng:

```text
email/username + password
```

Backend trả:

```text
access token + refresh token
```

### Lớp 2: Vault PIN/App Lock

Sau khi login, mobile app yêu cầu người dùng tạo PIN hoặc bật biometric. PIN chủ yếu bảo vệ ở phía thiết bị.

### Lớp 3: Re-auth từ backend

Khi user muốn xem dữ liệu CRITICAL như password, app gọi:

```http
POST /api/auth/re-auth
```

Backend cấp `reauthToken` sống ngắn, ví dụ 5 phút.

Sau đó mobile gửi:

```http
X-Reauth-Token: <token>
```

để reveal password hoặc download file rất nhạy cảm.

---

## 8.3 Mã hóa

### Dữ liệu cần mã hóa

| Dữ liệu | Cách lưu |
|---|---|
| Password đăng nhập | BCrypt hash |
| Refresh token | Hash |
| Password vault | AES encrypted |
| Username của password entry | AES encrypted |
| Note nhạy cảm | AES encrypted |
| Số CCCD | AES encrypted |
| Số BHYT | AES encrypted |
| Số hộ chiếu | AES encrypted |
| File SECRET/CRITICAL | Encrypted file |

### Khuyến nghị MVP

- Dùng AES-GCM cho field encryption.
- Key lấy từ biến môi trường `VAULT_ENCRYPTION_KEY`.
- Mỗi bản ghi/file nên có IV riêng.
- Không commit key lên GitHub.

---

# 9. PHÂN LOẠI DỮ LIỆU, TAG VÀ SECURITY LEVEL

## 9.1 Vault item type

| Type | Ví dụ | Security mặc định |
|---|---|---|
| `IDENTITY_DOCUMENT` | CCCD, hộ chiếu, giấy khai sinh | SECRET |
| `HEALTH_DOCUMENT` | BHYT, hồ sơ khám bệnh | CONFIDENTIAL |
| `EDUCATION_DOCUMENT` | Bằng cấp, chứng chỉ, bảng điểm | PRIVATE |
| `FINANCE_DOCUMENT` | Ngân hàng, mã số thuế | SECRET |
| `PASSWORD_ENTRY` | Google, Facebook, GitHub | CRITICAL |
| `PERSONAL_NOTE` | Mã sinh viên, ghi chú cá nhân | PRIVATE |
| `CONTRACT_DOCUMENT` | Hợp đồng thuê nhà, lao động | CONFIDENTIAL |
| `VEHICLE_DOCUMENT` | Bằng lái, đăng ký xe | CONFIDENTIAL |
| `WORK_PROFILE` | CV, portfolio | PRIVATE |
| `OTHER` | Dữ liệu khác | NORMAL |

---

## 9.2 Category

| Category | Mô tả |
|---|---|
| `IDENTITY` | Giấy tờ định danh |
| `HEALTH` | Y tế, bảo hiểm |
| `EDUCATION` | Học tập |
| `FINANCE` | Tài chính |
| `PASSWORD` | Tài khoản/mật khẩu |
| `CONTRACT` | Hợp đồng |
| `VEHICLE` | Xe cộ |
| `WORK` | Công việc |
| `PERSONAL` | Ghi chú cá nhân |
| `OTHER` | Khác |

---

## 9.3 Security level

| Level | Tên | Ví dụ | Yêu cầu |
|---|---|---|---|
| 1 | NORMAL | Ghi chú thường | Không cần re-auth |
| 2 | PRIVATE | CV, bảng điểm | Owner check |
| 3 | CONFIDENTIAL | BHYT, hợp đồng | Mã hóa field nhạy cảm |
| 4 | SECRET | CCCD, hộ chiếu | Mã hóa + audit khi xem/tải |
| 5 | CRITICAL | Password, PIN | Mã hóa + re-auth bắt buộc |

---

## 9.4 Tag gợi ý

```text
cccd
bhyt
passport
driver-license
bank
tax
student-code
degree
certificate
cv
github
google
facebook
insurance
important
favorite
expired
near-expired
school
work
healthcare
government
travel
```

---

# 10. YÊU CẦU CHỨC NĂNG

## 10.1 Auth module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| AUTH-001 | Đăng ký tài khoản | Cao |
| AUTH-002 | Đăng nhập | Cao |
| AUTH-003 | Sinh JWT access token | Cao |
| AUTH-004 | Sinh refresh token | Cao |
| AUTH-005 | Refresh access token | Cao |
| AUTH-006 | Logout và blacklist token | Cao |
| AUTH-007 | Đổi mật khẩu | Cao |
| AUTH-008 | Lấy thông tin user hiện tại | Cao |
| AUTH-009 | Quản lý session/thiết bị | Trung bình |
| AUTH-010 | Re-auth để xem dữ liệu nhạy cảm | Cao |
| AUTH-011 | Ghi login log | Cao |
| AUTH-012 | Khóa tạm sau nhiều lần sai password | Trung bình |

---

## 10.2 Gateway module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| GW-001 | Route `/api/auth/**` sang auth-service | Cao |
| GW-002 | Route `/api/vault/**` sang vault-service | Cao |
| GW-003 | Cho phép public endpoint login/register/health | Cao |
| GW-004 | Kiểm tra JWT với private endpoint | Cao |
| GW-005 | Kiểm tra Redis blacklist | Cao |
| GW-006 | Cấu hình CORS | Cao |
| GW-007 | Rate limit login/search nếu cần | Trung bình |
| GW-008 | Global error response | Trung bình |

---

## 10.3 Folder module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| FOLDER-001 | Tạo folder | Cao |
| FOLDER-002 | Sửa folder | Cao |
| FOLDER-003 | Xóa folder | Trung bình |
| FOLDER-004 | Lấy cây folder | Cao |
| FOLDER-005 | Di chuyển folder | Thấp |
| FOLDER-006 | Di chuyển item giữa folder | Trung bình |

---

## 10.4 Tag module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| TAG-001 | Tạo tag | Cao |
| TAG-002 | Sửa tag | Trung bình |
| TAG-003 | Xóa tag | Trung bình |
| TAG-004 | Gán tag cho item | Cao |
| TAG-005 | Lọc item theo tag | Cao |
| TAG-006 | Gợi ý tag theo type | Thấp |

---

## 10.5 Vault item module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| ITEM-001 | Tạo vault item | Cao |
| ITEM-002 | Sửa vault item | Cao |
| ITEM-003 | Xem danh sách item | Cao |
| ITEM-004 | Xem chi tiết item | Cao |
| ITEM-005 | Xóa mềm item | Cao |
| ITEM-006 | Khôi phục item | Trung bình |
| ITEM-007 | Xóa vĩnh viễn item | Trung bình |
| ITEM-008 | Favorite item | Trung bình |
| ITEM-009 | Archive item | Trung bình |
| ITEM-010 | Mask dữ liệu nhạy cảm trong list | Cao |
| ITEM-011 | Owner check mọi thao tác | Cao |

---

## 10.6 Password vault module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| PASS-001 | Tạo password entry | Cao |
| PASS-002 | Mã hóa username/password/note | Cao |
| PASS-003 | Hiển thị password dạng ẩn | Cao |
| PASS-004 | Reveal password sau re-auth | Cao |
| PASS-005 | Copy password và ghi audit log | Cao |
| PASS-006 | Cập nhật password entry | Cao |
| PASS-007 | Xóa password entry | Cao |
| PASS-008 | Sinh password mạnh | Thấp |

---

## 10.7 File module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| FILE-001 | Upload ảnh/PDF | Cao |
| FILE-002 | Validate MIME type/file size | Cao |
| FILE-003 | Tính checksum | Trung bình |
| FILE-004 | Lưu file local | Cao |
| FILE-005 | Mã hóa file SECRET/CRITICAL | Cao |
| FILE-006 | Download file | Cao |
| FILE-007 | Delete file | Cao |
| FILE-008 | Preview file | Trung bình |

---

## 10.8 Search module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| SEARCH-001 | Search keyword | Cao |
| SEARCH-002 | Filter theo category | Cao |
| SEARCH-003 | Filter theo type | Cao |
| SEARCH-004 | Filter theo tag | Cao |
| SEARCH-005 | Filter theo folder | Cao |
| SEARCH-006 | Filter theo status | Cao |
| SEARCH-007 | Filter theo near expired | Cao |
| SEARCH-008 | Pagination/sort | Cao |

---

## 10.9 Reminder module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| REM-001 | Nhập expiration date | Cao |
| REM-002 | Tạo reminder trước 30 ngày | Cao |
| REM-003 | Tạo reminder trước 7 ngày | Cao |
| REM-004 | Lấy danh sách reminder | Cao |
| REM-005 | Đánh dấu đã đọc | Trung bình |
| REM-006 | Gia hạn giấy tờ | Trung bình |

---

## 10.10 Audit log module

| Mã | Chức năng | Ưu tiên |
|---|---|---|
| LOG-001 | Log login success/failed | Cao |
| LOG-002 | Log create/update/delete item | Cao |
| LOG-003 | Log view secret item | Cao |
| LOG-004 | Log upload/download file | Cao |
| LOG-005 | Log reveal/copy password | Cao |
| LOG-006 | User xem audit log của mình | Trung bình |

---

# 11. USE CASE CHI TIẾT

## 11.1 UC-AUTH-01: Đăng ký

| Mục | Nội dung |
|---|---|
| Actor | Mobile User |
| Tiền điều kiện | User chưa có tài khoản |
| Hậu điều kiện | Tài khoản được tạo |

### Luồng chính

1. User mở màn đăng ký.
2. Nhập username, email, password.
3. Mobile gọi `POST /api/auth/register`.
4. Auth-service validate dữ liệu.
5. Kiểm tra username/email chưa tồn tại.
6. Hash password bằng BCrypt.
7. Tạo user với status `ACTIVE`.
8. Gán role `USER`.
9. Trả thông tin user đã tạo.

### Ngoại lệ

- Email đã tồn tại.
- Password yếu.
- Username không hợp lệ.
- Database lỗi.

---

## 11.2 UC-AUTH-02: Đăng nhập

| Mục | Nội dung |
|---|---|
| Actor | Mobile User |
| Tiền điều kiện | User đã có tài khoản |
| Hậu điều kiện | User nhận access token và refresh token |

### Luồng chính

1. User nhập username/email và password.
2. Mobile gọi `POST /api/auth/login`.
3. Auth-service tìm user.
4. Kiểm tra password bằng BCrypt.
5. Kiểm tra status.
6. Sinh access token.
7. Sinh refresh token.
8. Hash refresh token lưu vào `user_sessions`.
9. Ghi `login_logs`.
10. Trả token cho mobile.
11. Mobile lưu token vào secure storage.

### Ngoại lệ

- Sai password.
- Tài khoản bị khóa.
- Sai quá ngưỡng thì khóa tạm.

---

## 11.3 UC-ITEM-01: Tạo CCCD

| Mục | Nội dung |
|---|---|
| Actor | Mobile User |
| Tiền điều kiện | User đã login |
| Hậu điều kiện | CCCD được lưu an toàn |

### Luồng chính

1. User chọn tạo mới.
2. Chọn type `IDENTITY_DOCUMENT`.
3. Chọn template `CCCD`.
4. Nhập title, ngày cấp, ngày hết hạn.
5. Nhập số CCCD và nơi cấp.
6. Chọn folder.
7. Chọn tag.
8. Mobile gọi `POST /api/vault/items`.
9. Gateway validate JWT.
10. Vault-service lấy `currentUserCode`.
11. Validate dữ liệu.
12. Đặt security level mặc định `SECRET`.
13. Mã hóa số CCCD và thông tin nhạy cảm.
14. Lưu `vault_items`.
15. Lưu quan hệ tag.
16. Ghi audit log `CREATE_ITEM`.
17. Trả `itemCode`.

### Ngoại lệ

- Thiếu thông tin bắt buộc.
- Ngày hết hạn không hợp lệ.
- Folder không thuộc user.
- Tag không thuộc user.

---

## 11.4 UC-FILE-01: Upload ảnh CCCD

| Mục | Nội dung |
|---|---|
| Actor | Mobile User |
| Tiền điều kiện | Item CCCD đã tồn tại |
| Hậu điều kiện | File được lưu và gắn với item |

### Luồng chính

1. User mở item CCCD.
2. Chọn upload ảnh mặt trước.
3. Mobile gọi `POST /api/vault/items/{itemCode}/files`.
4. Gateway validate JWT.
5. Vault-service kiểm tra item thuộc user.
6. Validate file type/size.
7. Tính checksum.
8. Mã hóa file nếu item security level >= SECRET.
9. Lưu file vào local storage.
10. Lưu metadata vào `vault_files`.
11. Ghi audit log `UPLOAD_FILE`.

### Ngoại lệ

- File quá lớn.
- File không đúng định dạng.
- Storage lỗi.
- DB lỗi thì rollback/xóa file đã lưu.

---

## 11.5 UC-PASS-01: Lưu mật khẩu

| Mục | Nội dung |
|---|---|
| Actor | Mobile User |
| Tiền điều kiện | User đã login |
| Hậu điều kiện | Password được mã hóa |

### Luồng chính

1. User chọn tạo password.
2. Nhập service name, login URL, username, password, note.
3. Mobile gọi `POST /api/vault/passwords`.
4. Vault-service tạo `vault_items` type `PASSWORD_ENTRY`.
5. Đặt security level `CRITICAL`.
6. Mã hóa username/password/note.
7. Lưu `password_entries`.
8. Ghi audit log `CREATE_PASSWORD`.

---

## 11.6 UC-PASS-02: Reveal password

| Mục | Nội dung |
|---|---|
| Actor | Mobile User |
| Tiền điều kiện | Password entry thuộc user |
| Hậu điều kiện | Password được hiển thị tạm thời |

### Luồng chính

1. User mở password entry.
2. App hiển thị password dạng `********`.
3. User bấm hiện mật khẩu.
4. App yêu cầu PIN/biometric.
5. Mobile gọi `POST /api/auth/re-auth`.
6. Auth-service cấp `reauthToken` sống 5 phút.
7. Mobile gọi `POST /api/vault/passwords/{itemCode}/reveal`.
8. Vault-service kiểm tra owner.
9. Vault-service kiểm tra reauth token.
10. Vault-service giải mã password.
11. Ghi audit log `REVEAL_PASSWORD`.
12. Trả password cho mobile.

---

## 11.7 UC-SEARCH-01: Tìm kiếm item

| Mục | Nội dung |
|---|---|
| Actor | Mobile User |
| Tiền điều kiện | User đã login |
| Hậu điều kiện | Danh sách item đúng quyền |

### Luồng chính

1. User nhập keyword.
2. Mobile gọi `GET /api/vault/search?q=...`.
3. Vault-service filter theo `user_code`.
4. Tìm theo title, description, category, tag.
5. Mask dữ liệu nhạy cảm.
6. Trả danh sách phân trang.

---

## 11.8 UC-REM-01: Tạo reminder tự động

| Mục | Nội dung |
|---|---|
| Actor | System Scheduler |
| Tiền điều kiện | Item có expiration date |
| Hậu điều kiện | Reminder được tạo |

### Luồng chính

1. Scheduler chạy mỗi ngày.
2. Query item sắp hết hạn trong 30 ngày.
3. Query item sắp hết hạn trong 7 ngày.
4. Tạo reminder nếu chưa có.
5. Cập nhật trạng thái item thành `NEAR_EXPIRED` nếu cần.
6. Mobile hiển thị reminder trong dashboard.

---

# 12. LUỒNG TRẠNG THÁI

## 12.1 Vault item

```text
DRAFT -> ACTIVE -> NEAR_EXPIRED -> EXPIRED
ACTIVE -> FAVORITE
ACTIVE -> ARCHIVED
ACTIVE/ARCHIVED/EXPIRED -> DELETED -> PURGED
```

| Trạng thái | Ý nghĩa |
|---|---|
| `DRAFT` | Đang nhập nháp |
| `ACTIVE` | Đang sử dụng |
| `FAVORITE` | Đánh dấu quan trọng |
| `NEAR_EXPIRED` | Sắp hết hạn |
| `EXPIRED` | Đã hết hạn |
| `ARCHIVED` | Lưu trữ |
| `DELETED` | Xóa mềm |
| `PURGED` | Xóa vĩnh viễn |

---

## 12.2 File

```text
UPLOADING -> ACTIVE -> DELETED -> PURGED
ACTIVE -> REPLACED
```

| Trạng thái | Ý nghĩa |
|---|---|
| `UPLOADING` | Đang upload |
| `ACTIVE` | File đang dùng |
| `REPLACED` | Đã có bản mới thay thế |
| `DELETED` | Xóa mềm |
| `PURGED` | Xóa vật lý |

---

## 12.3 Session

```text
ACTIVE -> REVOKED
ACTIVE -> EXPIRED
```

| Trạng thái | Ý nghĩa |
|---|---|
| `ACTIVE` | Refresh token còn hiệu lực |
| `REVOKED` | User logout hoặc thu hồi |
| `EXPIRED` | Hết hạn |

---

## 12.4 Reminder

```text
PENDING -> SENT -> READ
PENDING -> DISMISSED
PENDING -> FAILED -> RETRY
```

| Trạng thái | Ý nghĩa |
|---|---|
| `PENDING` | Chờ gửi/hiển thị |
| `SENT` | Đã tạo/gửi |
| `READ` | User đã đọc |
| `DISMISSED` | User bỏ qua |
| `FAILED` | Gửi lỗi |
| `RETRY` | Chờ gửi lại |

---

# 13. THIẾT KẾ DATABASE

## 13.1 Tên database

```text
life_vault_db
```

## 13.2 Danh sách bảng MVP

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

Tổng cộng 13 bảng. Đây là số lượng hợp lý cho app cá nhân nhưng vẫn đủ nghiệp vụ bảo mật.

---

## 13.3 Quy tắc thiết kế database

1. Tất cả bảng dữ liệu cá nhân phải có `user_code`.
2. Mọi API vault phải query theo `user_code`.
3. Mỗi bảng vẫn có `id` BIGINT PK nội bộ, nhưng không dùng `id` để join nghiệp vụ.
4. Mọi quan hệ giữa bảng dùng `*_code`, ví dụ `user_code`, `role_code`, `folder_code`, `vault_item_code`, `tag_code`.
5. Không tạo foreign key vật lý trong MVP; service join bằng code và enforce nghiệp vụ ở application layer.
6. Không lưu plaintext password.
7. Không lưu plaintext số CCCD/BHYT/password vault.
8. Dữ liệu search nên nằm ở metadata không nhạy cảm.
9. Nội dung nhạy cảm lưu ở field encrypted.
10. Soft delete trước, purge sau.
11. Có index cho filter phổ biến.
12. Tag là many-to-many qua bảng `vault_item_tags`.
13. Folder dùng `parent_folder_code` để tạo cây.

## 13.4 Trường dùng chung

Tất cả bảng có các trường audit/workflow sau:

| Cột | Kiểu | Default | Ghi chú |
|---|---|---|---|
| `create_user` | VARCHAR(100) | NULL | Người tạo bản ghi |
| `create_date` | DATETIME | NULL | Ngày tạo bản ghi |
| `update_user` | VARCHAR(100) | NULL | Người cập nhật bản ghi |
| `update_date` | DATETIME | NULL | Ngày cập nhật bản ghi |
| `workflow_state` | VARCHAR(100) | `active` | Trạng thái hiện tại |
| `sync_source` | VARCHAR(300) | NULL | Nguồn đồng bộ |

---

# 14. PHÂN TÍCH TỪNG BẢNG

## 14.1 `users`

### Mục đích

Lưu tài khoản đăng nhập.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID user |
| `user_code` | VARCHAR(100) UNIQUE | Mã user dùng để join |
| `username` | VARCHAR(100) UNIQUE | Tên đăng nhập |
| `email` | VARCHAR(255) UNIQUE | Email |
| `password_hash` | VARCHAR(255) | BCrypt hash |
| `status` | VARCHAR(30) | ACTIVE, LOCKED, DISABLED |
| `failed_login_count` | INT | Số lần đăng nhập sai |
| `locked_until` | DATETIME(6) NULL | Khóa tạm |
| `last_login_at` | DATETIME(6) NULL | Lần login cuối |

### Index

```sql
UNIQUE KEY uk_users_user_code (user_code);
UNIQUE KEY uk_users_username (username);
UNIQUE KEY uk_users_email (email);
KEY idx_users_status (status);
```

---

## 14.2 `roles`

### Mục đích

Lưu role hệ thống.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID role |
| `role_code` | VARCHAR(50) UNIQUE | USER, ADMIN |
| `name` | VARCHAR(100) | Tên hiển thị |

MVP chỉ cần role `USER`.

---

## 14.3 `user_roles`

### Mục đích

Liên kết user và role.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `user_code` | VARCHAR(100) PK | Join tới `users.user_code` |
| `role_code` | VARCHAR(50) PK | Join tới `roles.role_code` |

Primary key:

```sql
PRIMARY KEY (user_code, role_code);
KEY idx_user_roles_role_code (role_code);
```

---

## 14.4 `user_sessions`

### Mục đích

Quản lý refresh token và thiết bị đăng nhập.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID session |
| `session_code` | VARCHAR(100) UNIQUE | Mã session dùng cho API |
| `user_code` | VARCHAR(100) | Join tới `users.user_code` |
| `refresh_token_hash` | VARCHAR(255) | Hash refresh token |
| `device_id` | VARCHAR(255) | ID thiết bị |
| `device_name` | VARCHAR(255) | Tên thiết bị |
| `ip_address` | VARCHAR(100) | IP |
| `user_agent` | TEXT | User agent |
| `status` | VARCHAR(30) | ACTIVE, REVOKED, EXPIRED |
| `expires_at` | DATETIME(6) | Hạn |
| `revoked_at` | DATETIME(6) NULL | Thời điểm revoke |

### Lý do cần bảng này

- Cho phép logout từng thiết bị.
- Cho phép refresh token an toàn.
- Cho phép xem lịch sử phiên đăng nhập.
- Cho phép revoke token khi nghi ngờ bị lộ.

---

## 14.5 `login_logs`

### Mục đích

Ghi lịch sử đăng nhập.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID |
| `login_log_code` | VARCHAR(100) UNIQUE | Mã log |
| `user_code` | VARCHAR(100) NULL | Null nếu login sai username |
| `username_input` | VARCHAR(255) | Username/email đã nhập |
| `success` | BOOLEAN | Thành công/thất bại |
| `failure_reason` | VARCHAR(255) | Lý do lỗi |
| `ip_address` | VARCHAR(100) | IP |
| `user_agent` | TEXT | User agent |

---

## 14.6 `folders`

### Mục đích

Lưu folder phân cấp của từng user.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID folder |
| `folder_code` | VARCHAR(100) UNIQUE | Mã folder dùng cho API/join |
| `user_code` | VARCHAR(100) | Chủ sở hữu |
| `parent_folder_code` | VARCHAR(100) NULL | Folder cha |
| `name` | VARCHAR(255) | Tên folder |
| `path` | VARCHAR(1000) | Đường dẫn cache |
| `sort_order` | INT | Thứ tự |
| `deleted_at` | DATETIME(6) NULL | Xóa mềm |

### Ví dụ folder

```text
Giấy tờ cá nhân
  ├── CCCD
  ├── BHYT
  └── Bằng lái

Tài khoản
  ├── Công việc
  └── Cá nhân
```

### Index

```sql
KEY idx_folders_user_parent (user_code, parent_folder_code);
KEY idx_folders_user_name (user_code, name);
KEY idx_folders_workflow_state (workflow_state);
```

---

## 14.7 `tags`

### Mục đích

Lưu tag cá nhân.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID tag |
| `tag_code` | VARCHAR(100) UNIQUE | Mã tag dùng cho API/join |
| `user_code` | VARCHAR(100) | Chủ sở hữu |
| `name` | VARCHAR(100) | Tên tag |
| `color` | VARCHAR(20) | Màu |

### Constraint

```sql
UNIQUE KEY uk_tags_user_name (user_code, name);
KEY idx_tags_user_code (user_code);
```

---

## 14.8 `vault_items`

### Mục đích

Bảng trung tâm lưu mọi item trong vault.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID item |
| `vault_item_code` | VARCHAR(100) UNIQUE | Mã item dùng cho API/join |
| `user_code` | VARCHAR(100) | Chủ sở hữu |
| `folder_code` | VARCHAR(100) NULL | Join tới `folders.folder_code` |
| `title` | VARCHAR(500) | Tên item |
| `type` | VARCHAR(50) | Loại item |
| `category` | VARCHAR(50) | Nhóm dữ liệu |
| `security_level` | VARCHAR(30) | NORMAL, SECRET, CRITICAL |
| `description` | TEXT | Mô tả không quá nhạy cảm |
| `metadata_json` | TEXT/JSON | Metadata không nhạy cảm |
| `encrypted_content` | TEXT | Nội dung nhạy cảm mã hóa |
| `encryption_iv` | VARCHAR(255) | IV cho encrypted_content |
| `expiration_date` | DATE NULL | Ngày hết hạn |
| `status` | VARCHAR(30) | ACTIVE, DELETED |
| `is_favorite` | BOOLEAN | Yêu thích |
| `deleted_at` | DATETIME(6) NULL | Xóa mềm |

### Quy tắc

- `title`, `category`, `type`, `description` dùng để search.
- Không lưu số CCCD/BHYT/password plaintext trong `metadata_json`.
- Dữ liệu nhạy cảm nằm trong `encrypted_content`.

### Ví dụ metadata an toàn

```json
{
  "documentTemplate": "CCCD",
  "issuedDate": "2023-01-01",
  "hasFrontImage": true,
  "hasBackImage": true
}
```

### Ví dụ nội dung sau giải mã

```json
{
  "identityNumber": "001205xxxxxx",
  "issuedPlace": "Cục Cảnh sát QLHC",
  "fullNameOnDocument": "Nguyen Van A"
}
```

---

## 14.9 `vault_item_tags`

### Mục đích

Quan hệ many-to-many giữa item và tag.

| Cột | Kiểu |
|---|---|
| `vault_item_code` | VARCHAR(100) |
| `tag_code` | VARCHAR(100) |

Primary key:

```sql
PRIMARY KEY (vault_item_code, tag_code);
KEY idx_vault_item_tags_tag_code (tag_code);
```

---

## 14.10 `vault_files`

### Mục đích

Lưu metadata file.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID file |
| `file_code` | VARCHAR(100) UNIQUE | Mã file dùng cho API |
| `vault_item_code` | VARCHAR(100) | Join tới `vault_items.vault_item_code` |
| `user_code` | VARCHAR(100) | Chủ sở hữu |
| `original_filename` | VARCHAR(500) | Tên gốc |
| `stored_filename` | VARCHAR(500) | Tên lưu |
| `file_path` | VARCHAR(1000) | Đường dẫn lưu |
| `mime_type` | VARCHAR(100) | MIME type |
| `file_size` | BIGINT | Dung lượng |
| `checksum` | VARCHAR(255) | SHA-256 |
| `is_encrypted` | BOOLEAN | Đã mã hóa chưa |
| `encryption_iv` | VARCHAR(255) NULL | IV file |
| `status` | VARCHAR(30) | ACTIVE, DELETED |
| `deleted_at` | DATETIME(6) NULL | Xóa mềm |

### Quy tắc

- Không public `file_path`.
- Download luôn đi qua API.
- Kiểm tra `user_code` khi download.
- File SECRET/CRITICAL phải mã hóa.

---

## 14.11 `password_entries`

### Mục đích

Lưu thông tin riêng cho item loại password.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID |
| `password_entry_code` | VARCHAR(100) UNIQUE | Mã password entry |
| `vault_item_code` | VARCHAR(100) UNIQUE | Join tới `vault_items.vault_item_code` |
| `service_name` | VARCHAR(255) | Tên dịch vụ |
| `login_url` | VARCHAR(1000) | URL đăng nhập |
| `username_encrypted` | TEXT | Username mã hóa |
| `username_iv` | VARCHAR(255) | IV |
| `password_encrypted` | TEXT | Password mã hóa |
| `password_iv` | VARCHAR(255) | IV |
| `note_encrypted` | TEXT | Note mã hóa |
| `note_iv` | VARCHAR(255) | IV |
| `last_changed_at` | DATETIME(6) NULL | Lần đổi mật khẩu |

---

## 14.12 `reminders`

### Mục đích

Lưu nhắc nhở hết hạn.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID |
| `reminder_code` | VARCHAR(100) UNIQUE | Mã reminder |
| `vault_item_code` | VARCHAR(100) | Join tới `vault_items.vault_item_code` |
| `user_code` | VARCHAR(100) | User |
| `remind_at` | DATETIME(6) | Thời điểm nhắc |
| `reminder_type` | VARCHAR(50) | EXPIRE_30_DAYS, EXPIRE_7_DAYS |
| `status` | VARCHAR(30) | PENDING, SENT, READ, DISMISSED |
| `sent_at` | DATETIME(6) NULL | Đã gửi |

---

## 14.13 `audit_logs`

### Mục đích

Ghi log hành động nhạy cảm.

| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | BIGINT PK | ID |
| `audit_log_code` | VARCHAR(100) UNIQUE | Mã audit log |
| `user_code` | VARCHAR(100) | Người thực hiện |
| `action_type` | VARCHAR(100) | VIEW_SECRET_ITEM, REVEAL_PASSWORD |
| `target_type` | VARCHAR(100) | VAULT_ITEM, FILE, PASSWORD |
| `target_code` | VARCHAR(100) NULL | Mã đối tượng tác động |
| `ip_address` | VARCHAR(100) | IP |
| `user_agent` | TEXT | User agent |
| `success` | BOOLEAN | Thành công/thất bại |
| `failure_reason` | VARCHAR(255) NULL | Lý do lỗi |

### Action type

```text
LOGIN_SUCCESS
LOGIN_FAILED
LOGOUT
CREATE_ITEM
UPDATE_ITEM
DELETE_ITEM
PURGE_ITEM
VIEW_SECRET_ITEM
UPLOAD_FILE
DOWNLOAD_FILE
REVEAL_PASSWORD
COPY_PASSWORD
CHANGE_PASSWORD
REFRESH_TOKEN
```

---

# 15. API ENDPOINT THEO MODULE

Tất cả endpoint đi qua gateway với prefix `/api`.

---

## 15.1 Auth API

### POST `/api/auth/register`

Đăng ký.

Request:

```json
{
  "username": "linh",
  "email": "linh@example.com",
  "password": "StrongPassword@123"
}
```

Response:

```json
{
  "code": "200",
  "message": "Register successfully",
  "data": {
    "userCode": "USR-000001",
    "username": "linh",
    "email": "linh@example.com"
  }
}
```

---

### POST `/api/auth/login`

Đăng nhập.

Request:

```json
{
  "usernameOrEmail": "linh",
  "password": "StrongPassword@123",
  "deviceName": "OPPO A53",
  "deviceId": "mobile-device-id"
}
```

Response:

```json
{
  "code": "200",
  "message": "Login successfully",
  "data": {
    "accessToken": "access-token",
    "refreshToken": "refresh-token",
    "expiresIn": 900,
    "user": {
      "userCode": "USR-000001",
      "username": "linh",
      "email": "linh@example.com"
    }
  }
}
```

---

### POST `/api/auth/refresh`

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "code": "200",
  "data": {
    "accessToken": "new-access-token",
    "expiresIn": 900
  }
}
```

---

### POST `/api/auth/logout`

Header:

```http
Authorization: Bearer <access_token>
```

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Xử lý:

- Blacklist access token vào Redis.
- Revoke refresh token trong `user_sessions`.

---

### GET `/api/auth/me`

Response:

```json
{
  "code": "200",
  "data": {
    "userCode": "USR-000001",
    "username": "linh",
    "email": "linh@example.com",
    "status": "ACTIVE"
  }
}
```

---

### POST `/api/auth/change-password`

Request:

```json
{
  "oldPassword": "oldPassword",
  "newPassword": "newStrongPassword@123"
}
```

---

### POST `/api/auth/re-auth`

Cấp reauth token ngắn hạn.

Request:

```json
{
  "password": "currentPassword"
}
```

Response:

```json
{
  "code": "200",
  "data": {
    "reauthToken": "short-lived-token",
    "expiresIn": 300
  }
}
```

---

### GET `/api/auth/sessions`

Lấy danh sách phiên đăng nhập.

---

### POST `/api/auth/sessions/{sessionCode}/revoke`

Thu hồi một phiên.

---

## 15.2 Folder API

### GET `/api/vault/folders`

Lấy cây folder.

### POST `/api/vault/folders`

Request:

```json
{
  "name": "Giấy tờ cá nhân",
  "parentFolderCode": null
}
```

### PUT `/api/vault/folders/{folderCode}`

Request:

```json
{
  "name": "Tài liệu cá nhân",
  "parentFolderCode": null
}
```

### DELETE `/api/vault/folders/{folderCode}`

Quy tắc:

- Chỉ xóa folder thuộc user.
- Nếu còn item, có thể từ chối hoặc yêu cầu move item.

---

## 15.3 Tag API

### GET `/api/vault/tags`

Lấy tag của user.

### POST `/api/vault/tags`

Request:

```json
{
  "name": "cccd",
  "color": "#FFAA00"
}
```

### PUT `/api/vault/tags/{tagCode}`

### DELETE `/api/vault/tags/{tagCode}`

---

## 15.4 Vault Item API

### GET `/api/vault/items`

Query params:

```text
q
type
category
folderCode
tagCode
securityLevel
status
favorite
nearExpired
page
size
sort
```

---

### POST `/api/vault/items`

Tạo item.

Request ví dụ CCCD:

```json
{
  "title": "Căn cước công dân",
  "type": "IDENTITY_DOCUMENT",
  "category": "IDENTITY",
  "folderCode": "FLD-IDENTITY",
  "securityLevel": "SECRET",
  "description": "CCCD của tôi",
  "metadata": {
    "documentTemplate": "CCCD",
    "issuedDate": "2023-01-01",
    "expirationDate": "2038-01-01"
  },
  "sensitiveContent": {
    "identityNumber": "001205xxxxxx",
    "issuedPlace": "Cục Cảnh sát QLHC"
  },
  "tagCodes": ["TAG-CCCD", "TAG-IDENTITY"]
}
```

---

### GET `/api/vault/items/{itemCode}`

Lấy chi tiết item.

Quy tắc:

- Owner check.
- SECRET/CRITICAL ghi audit log.
- Có thể yêu cầu re-auth nếu trả sensitive content.

---

### PUT `/api/vault/items/{itemCode}`

Cập nhật item.

---

### DELETE `/api/vault/items/{itemCode}`

Xóa mềm.

---

### POST `/api/vault/items/{itemCode}/restore`

Khôi phục.

---

### DELETE `/api/vault/items/{itemCode}/purge`

Xóa vĩnh viễn.

Header:

```http
X-Reauth-Token: <reauth-token>
```

---

### POST `/api/vault/items/{itemCode}/favorite`

Đánh dấu favorite.

---

### POST `/api/vault/items/{itemCode}/archive`

Archive item.

---

## 15.5 Password API

### POST `/api/vault/passwords`

Request:

```json
{
  "title": "GitHub",
  "folderCode": "FLD-PASSWORDS",
  "serviceName": "GitHub",
  "loginUrl": "https://github.com/login",
  "username": "mygithub",
  "password": "MyPassword@123",
  "note": "Tài khoản dùng cho project",
  "tagCodes": ["TAG-WORK", "TAG-ACCOUNT"]
}
```

---

### GET `/api/vault/passwords/{itemCode}`

Không trả password plaintext.

Response:

```json
{
  "code": "200",
  "data": {
    "itemCode": "VIT-GITHUB",
    "serviceName": "GitHub",
    "loginUrl": "https://github.com/login",
    "usernameMasked": "myg***",
    "passwordMasked": "********",
    "securityLevel": "CRITICAL"
  }
}
```

---

### POST `/api/vault/passwords/{itemCode}/reveal`

Header:

```http
X-Reauth-Token: <reauth-token>
```

Response:

```json
{
  "code": "200",
  "data": {
    "username": "mygithub",
    "password": "MyPassword@123",
    "note": "Tài khoản dùng cho project"
  }
}
```

---

### PUT `/api/vault/passwords/{itemCode}`

Cập nhật password entry.

---

### POST `/api/vault/passwords/{itemCode}/copy`

Ghi audit log copy password.

---

## 15.6 File API

### POST `/api/vault/items/{itemCode}/files`

Upload multipart.

Fields:

```text
file
fileType: FRONT_IMAGE/BACK_IMAGE/PDF/OTHER
```

---

### GET `/api/vault/items/{itemCode}/files`

Lấy danh sách file.

---

### GET `/api/vault/files/{fileCode}/download`

Download file.

Quy tắc:

- Owner check.
- File SECRET/CRITICAL cần re-auth.
- Ghi audit log.

---

### DELETE `/api/vault/files/{fileCode}`

Xóa mềm file.

---

## 15.7 Search API

### GET `/api/vault/search`

Query params:

```text
q
category
type
tagCode
folderCode
securityLevel
status
nearExpired
fromDate
toDate
page
size
```

---

## 15.8 Reminder API

### GET `/api/vault/reminders`

Lấy reminder.

### POST `/api/vault/reminders/{reminderCode}/read`

Đánh dấu đã đọc.

### POST `/api/vault/reminders/{reminderCode}/dismiss`

Bỏ qua reminder.

### POST `/api/vault/items/{itemCode}/renew`

Gia hạn giấy tờ.

Request:

```json
{
  "newExpirationDate": "2030-01-01"
}
```

---

## 15.9 Audit Log API

### GET `/api/vault/audit-logs`

Query params:

```text
actionType
fromDate
toDate
page
size
```

---

# 16. LUỒNG NGHIỆP VỤ CHÍNH

## 16.1 Luồng đăng nhập

```text
Mobile App
  |
  | POST /api/auth/login
  v
API Gateway
  |
  v
Auth Service
  |
  | kiểm tra user/password
  | sinh access token
  | sinh refresh token
  | lưu user_sessions
  | ghi login_logs
  v
Mobile App lưu token trong secure storage
```

---

## 16.2 Luồng request vault

```text
Mobile App
  |
  | Authorization: Bearer token
  v
API Gateway
  |
  | validate JWT
  | check Redis blacklist
  v
Vault Service
  |
  | lấy currentUserCode
  | query WHERE user_code = currentUserCode
  v
Response
```

---

## 16.3 Luồng tạo item CCCD

```text
Mobile App
  |
  | POST /api/vault/items
  v
Gateway
  |
  v
Vault Service
  |
  | validate input
  | owner = currentUserCode
  | set type = IDENTITY_DOCUMENT
  | set security = SECRET
  | encrypt sensitiveContent
  | save vault_items
  | save vault_item_tags
  | audit CREATE_ITEM
  v
Response itemCode
```

---

## 16.4 Luồng upload file

```text
Mobile App chọn ảnh/PDF
  |
  | POST /api/vault/items/{itemCode}/files
  v
Vault Service
  |
  | check owner
  | validate file
  | checksum
  | encrypt file nếu cần
  | save local storage
  | save vault_files
  | audit UPLOAD_FILE
```

---

## 16.5 Luồng reveal password

```text
User bấm hiện mật khẩu
  |
  v
Mobile yêu cầu PIN/biometric
  |
  v
POST /api/auth/re-auth
  |
  v
Mobile nhận reauthToken
  |
  v
POST /api/vault/passwords/{itemCode}/reveal
  |
  v
Vault Service
  |
  | check owner
  | check reauth token
  | decrypt password
  | audit REVEAL_PASSWORD
  v
Mobile hiển thị password tạm thời
```

---

## 16.6 Luồng logout

```text
Mobile App
  |
  | POST /api/auth/logout
  v
Auth Service
  |
  | revoke refresh token
  | blacklist access token vào Redis
  v
Mobile xóa token khỏi secure storage
```

---

# 17. CẤU TRÚC SOURCE CODE CHI TIẾT

## 17.1 Root

```text
personal-life-vault
├── pom.xml
├── docker-compose.yml
├── .env
├── .env.example
├── README.md
├── docs
│   └── srs.md
├── common-lib
├── api-gateway
├── auth-service
└── vault-service
```

---

## 17.2 Thứ tự tạo module

1. `common-lib`
2. `auth-service`
3. `api-gateway`
4. `vault-service`

Lý do:

- `common-lib` dùng chung.
- `auth-service` cần xong trước để có JWT.
- `api-gateway` cần route auth trước.
- `vault-service` cần token để lấy current user.

---

# 18. KẾ HOẠCH TRIỂN KHAI TUẦN TỰ

## Phase 0: Khởi tạo project

Việc cần làm:

1. Tạo parent Maven project.
2. Tạo module `common-lib`.
3. Tạo module `auth-service`.
4. Tạo module `api-gateway`.
5. Tạo module `vault-service`.
6. Tạo `.gitignore`.
7. Tạo `.env.example`.
8. Tạo Docker Compose MariaDB + Redis.
9. Tạo README ban đầu.

Kết quả:

```text
Repo chạy được skeleton project
```

---

## Phase 1: Thiết kế database

Việc cần làm:

1. Chốt 13 bảng.
2. Vẽ ERD.
3. Tạo migration hoặc SQL init.
4. Tạo entity JPA.
5. Tạo index.
6. Tạo seed role USER.

Kết quả:

```text
Database schema v1 hoàn chỉnh
```

---

## Phase 2: Xây auth-service

Việc cần làm:

1. User entity/repository.
2. Role entity/repository.
3. UserSession.
4. LoginLog.
5. Register.
6. Login.
7. JWT.
8. Refresh token.
9. Logout.
10. Re-auth.
11. Change password.
12. Swagger.

Kết quả:

```text
Auth-service hoạt động độc lập
```

---

## Phase 3: Xây api-gateway

Việc cần làm:

1. Route auth.
2. Route vault.
3. JWT global filter.
4. Redis blacklist check.
5. CORS.
6. Error response.
7. Actuator health.

Kết quả:

```text
Mobile chỉ cần gọi gateway
```

---

## Phase 4: Xây vault-service core

Việc cần làm:

1. Folder CRUD.
2. Tag CRUD.
3. VaultItem CRUD.
4. OwnerCheckService.
5. Search cơ bản.
6. Mask response.
7. Audit log create/update/delete.

Kết quả:

```text
Tạo được dữ liệu cá nhân cơ bản
```

---

## Phase 5: File upload/download

Việc cần làm:

1. Local storage config.
2. Upload multipart.
3. Validate file.
4. Checksum.
5. Download.
6. Delete file.
7. Audit log.

Kết quả:

```text
Lưu được ảnh/PDF giấy tờ
```

---

## Phase 6: Encryption

Việc cần làm:

1. EncryptionService.
2. AES-GCM field encryption.
3. File encryption.
4. MaskingUtil.
5. Không log plaintext.
6. Test database.

Kết quả:

```text
Dữ liệu nhạy cảm không đọc được trực tiếp từ DB
```

---

## Phase 7: Password Vault

Việc cần làm:

1. Create password entry.
2. Update password entry.
3. Reveal password.
4. Re-auth token.
5. Copy password audit.
6. Mask username/password.

Kết quả:

```text
Password vault dùng được
```

---

## Phase 8: Reminder + Audit

Việc cần làm:

1. Reminder scheduler.
2. Reminder API.
3. Renew item.
4. AuditLog API.
5. AOP audit annotation.

Kết quả:

```text
Có nhắc hết hạn và lịch sử bảo mật
```

---

## Phase 9: Mobile app

Việc cần làm:

1. Login/Register UI.
2. Secure token storage.
3. App Lock/Vault PIN.
4. Home dashboard.
5. Folder UI.
6. Item list/detail.
7. Create item.
8. Upload file.
9. Password vault.
10. Search.
11. Reminder.
12. Audit log.

Kết quả:

```text
App chạy end-to-end
```

---

# 19. ROADMAP MVP THEO TUẦN

| Tuần | Việc chính | Kết quả |
|---|---|---|
| 1 | Tạo repo, parent POM, Docker, common-lib | Project skeleton |
| 2 | Database auth + auth-service register/login | Login được |
| 3 | Refresh/logout/re-auth + gateway route | Auth qua gateway |
| 4 | Database vault + folder/tag | Tạo folder/tag |
| 5 | Vault item CRUD + owner check | Lưu item |
| 6 | Search/filter + audit cơ bản | Tìm kiếm được |
| 7 | File upload/download | Lưu file giấy tờ |
| 8 | Encryption field/file | Bảo mật dữ liệu |
| 9 | Password vault | Lưu/xem password |
| 10 | Reminder scheduler | Nhắc hết hạn |
| 11 | Mobile app cơ bản | Login/list/create |
| 12 | Hoàn thiện mobile + README + demo | MVP hoàn chỉnh |

---

# 20. TIÊU CHÍ NGHIỆM THU

| Mã | Tiêu chí | Cách kiểm tra |
|---|---|---|
| AC-001 | User chưa login không gọi được vault API | Gọi API không token trả 401 |
| AC-002 | User A không xem được item của User B | Đổi itemCode trả 403/404 |
| AC-003 | Password đăng nhập không lưu plaintext | Kiểm tra DB |
| AC-004 | Refresh token không lưu plaintext | Kiểm tra DB |
| AC-005 | Password vault không lưu plaintext | Kiểm tra DB |
| AC-006 | Số CCCD/BHYT không lưu plaintext | Kiểm tra DB |
| AC-007 | Logout xong token cũ không dùng được | Gọi API bằng token cũ |
| AC-008 | Upload file hợp lệ thành công | Upload ảnh/PDF |
| AC-009 | File SECRET được mã hóa | Kiểm tra storage |
| AC-010 | Reveal password yêu cầu re-auth | Gọi API không reauth token |
| AC-011 | Download file ghi audit log | Kiểm tra audit_logs |
| AC-012 | Search theo tag/category đúng | Tạo item rồi search |
| AC-013 | Reminder tạo khi gần hết hạn | Tạo item expiration gần |
| AC-014 | API list không lộ dữ liệu nhạy cảm | Kiểm tra response |
| AC-015 | Gateway chặn token blacklist | Logout rồi gọi API |

---

# 21. OPEN ISSUES

| Câu hỏi | Khuyến nghị |
|---|---|
| Dùng MariaDB hay PostgreSQL? | MariaDB dễ bắt đầu và tương thích tốt với schema hiện tại; PostgreSQL tốt nếu dùng JSONB |
| Có dùng master password không? | MVP dùng Vault PIN + re-auth trước |
| Mã hóa toàn bộ file không? | File SECRET/CRITICAL nên mã hóa |
| Có cho lưu mã PIN ngân hàng không? | Cho phép nhưng cảnh báo CRITICAL |
| Có push notification không? | Giai đoạn 2 |
| Có OCR không? | Giai đoạn 3 |
| Có export backup không? | Giai đoạn 2, phải export mã hóa |
| Có tách file-service không? | Không ở MVP |
| Có tách notification-service không? | Không ở MVP |
| Admin có xem dữ liệu user không? | Không |

---

# 22. KẾT LUẬN

Personal Life Vault là một mobile app cá nhân có giá trị thực tế cao. Với kiến trúc nhỏ gồm:

```text
api-gateway
auth-service
vault-service
1 SQL database
Redis
```

hệ thống vẫn đáp ứng đầy đủ các nghiệp vụ quan trọng:

- Đăng nhập an toàn.
- Quản lý session/token.
- Lưu giấy tờ cá nhân.
- Lưu mật khẩu.
- Upload file.
- Mã hóa dữ liệu nhạy cảm.
- Tìm kiếm nhanh.
- Phân loại folder/tag.
- Nhắc hết hạn.
- Audit log.

Thứ tự triển khai khuyến nghị:

```text
Database -> Auth Service -> API Gateway -> Vault Core -> File Upload -> Encryption -> Password Vault -> Reminder -> Mobile App
```

Kiến trúc này đủ nhỏ để hoàn thành MVP, nhưng vẫn đủ tốt để mở rộng thành sản phẩm thực tế trong tương lai.
