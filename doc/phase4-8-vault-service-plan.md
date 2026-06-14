# Phase 4-8 Vault Service Backend Plan

## Goal

Complete the remaining backend phases from the SRS for `vault-service`: personal vault CRUD, file storage, encryption, password vault, reminders, audit logs, and search. Endpoints are exposed under `/vault/**` so the gateway exposes them as `/api/vault/**`.

## Phase 4: Vault Core

| Step | Status | Note |
|---|---|---|
| Folder CRUD | Done | `/vault/folders` |
| Tag CRUD | Done | `/vault/tags` |
| VaultItem CRUD | Done | `/vault/items` |
| Owner check | Done | All repositories filter by `user_code` from JWT subject |
| Search/filter | Done | `/vault/items` and `/vault/search` |
| Mask response | Done | SECRET/CRITICAL sensitive content is not returned in item detail |
| Audit create/update/delete | Done | Manual audit writes in service layer |

## Phase 5: File Upload/Download

| Step | Status | Note |
|---|---|---|
| Local storage config | Done | `vault.storage.root` |
| Multipart upload | Done | `POST /vault/items/{itemCode}/files` |
| Validate file | Done | Size and MIME allow-list |
| Checksum | Done | SHA-256 |
| Download | Done | `GET /vault/files/{fileCode}/download` |
| Delete file | Done | Soft delete |
| Audit log | Done | Upload/download/delete |

## Phase 6: Encryption

| Step | Status | Note |
|---|---|---|
| EncryptionService | Done | AES-GCM |
| Field encryption | Done | Vault item sensitive content and password fields |
| File encryption | Done | Files on SECRET/CRITICAL items |
| MaskingUtil | Done | Username/password masking |
| No plaintext password in DB | Done | Password vault fields encrypted |

## Phase 7: Password Vault

| Step | Status | Note |
|---|---|---|
| Create password entry | Done | `POST /vault/passwords` |
| Update password entry | Done | `PUT /vault/passwords/{itemCode}` |
| Detail masked | Done | `GET /vault/passwords/{itemCode}` |
| Reveal password | Done | Requires `X-Reauth-Token` |
| Copy password audit | Done | `POST /vault/passwords/{itemCode}/copy` |

## Phase 8: Reminder + Audit

| Step | Status | Note |
|---|---|---|
| Reminder scheduler | Done | Marks due reminders as SENT |
| Reminder API | Done | list/read/dismiss |
| Renew item | Done | `POST /vault/items/{itemCode}/renew` |
| AuditLog API | Done | `/vault/audit-logs` |
| Audit annotation | Deferred | Manual service-layer audit is implemented for MVP |

## Verification

Smoke test covered:

```text
POST /auth/register -> 200
POST /auth/login -> 200
POST /vault/folders -> 200
POST /vault/tags -> 200
POST /vault/items -> 200
GET  /vault/items -> 200
POST /vault/passwords -> 200
POST /auth/re-auth -> 200
POST /vault/passwords/{itemCode}/reveal -> 200
GET  /vault/reminders -> 200
GET  /vault/audit-logs -> 200
```

## Local Run

```text
cd vault-service
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The gateway exposes these routes as `/api/vault/**`.
