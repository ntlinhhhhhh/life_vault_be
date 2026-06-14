# Phase 2 Auth Service Plan

## Mục tiêu

Xây `auth-service` hoạt động độc lập theo SRS: quản lý user, role, session, login log, JWT, refresh token, logout, re-auth, change password và Swagger.

## Checklist

| Bước | Trạng thái | Ghi chú |
|---|---|---|
| User entity/repository | Done | Entity từ Phase 1, thêm `UserRepository` |
| Role entity/repository | Done | Entity từ Phase 1, thêm `RoleRepository`, auto đảm bảo role `USER` khi register |
| UserSession | Done | Entity từ Phase 1, thêm repository và API session |
| LoginLog | Done | Ghi log login thành công/thất bại |
| Register | Done | `POST /auth/register`, tạo `user_code`, hash password, gán role USER |
| Login | Done | `POST /auth/login`, kiểm tra password, cấp access token và refresh token |
| JWT | Done | Access token JWT, re-auth token JWT |
| Refresh token | Done | Refresh token opaque, lưu SHA-256 trong `user_sessions` |
| Logout | Done | Revoke refresh token, blacklist access token qua Redis nếu Redis sẵn sàng |
| Re-auth | Done | `POST /auth/re-auth`, xác thực password và trả token ngắn hạn |
| Change password | Done | `POST /auth/change-password`, yêu cầu JWT |
| Session management | Done | `GET /auth/sessions`, `POST /auth/sessions/{sessionCode}/revoke` |
| Swagger | Done | Springdoc OpenAPI tại `/swagger-ui.html` |

## Endpoint

```text
POST /auth/register
POST /auth/login
POST /auth/refresh
POST /auth/logout
GET  /auth/me
POST /auth/change-password
POST /auth/re-auth
GET  /auth/sessions
POST /auth/sessions/{sessionCode}/revoke
```

## Token

- Access token: JWT, TTL `AUTH_ACCESS_TOKEN_TTL_SECONDS`.
- Refresh token: random opaque token, chỉ lưu SHA-256 trong database.
- Re-auth token: JWT ngắn hạn, TTL `AUTH_REAUTH_TOKEN_TTL_SECONDS`.

## Cấu hình môi trường

Các biến đã thêm vào `.env.example`:

```text
AUTH_JWT_SECRET
AUTH_ACCESS_TOKEN_TTL_SECONDS
AUTH_REAUTH_TOKEN_TTL_SECONDS
AUTH_REFRESH_TOKEN_TTL_SECONDS
AUTH_MAX_FAILED_LOGIN_COUNT
AUTH_LOCK_DURATION_MINUTES
```
