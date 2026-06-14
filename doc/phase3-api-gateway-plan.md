# Phase 3 API Gateway Plan

## Goal

Build `api-gateway` as the single mobile entry point from the SRS. Gateway routes auth and vault APIs, validates access tokens for private APIs, checks the Redis token blacklist when available, exposes CORS, and returns normalized gateway errors.

## Checklist

| Step | Status | Note |
|---|---|---|
| Route auth | Done | `/api/auth/**` proxies to `AUTH_SERVICE_URL`, stripping `/api` |
| Route vault | Done | `/api/vault/**` proxies to `VAULT_SERVICE_URL`, stripping `/api` |
| Public endpoints | Done | `POST /api/auth/register`, `POST /api/auth/login`, `POST /api/auth/refresh`, `OPTIONS /**`, `/actuator/**` |
| JWT validation | Done | Private `/api/auth/**` and `/api/vault/**` require Bearer access token |
| Redis blacklist check | Done | Uses `auth:blacklist:<token>` with local fail-open backoff |
| CORS | Done | Configurable allowed origins/methods/headers |
| Error response | Done | Gateway errors use `{ respCode, message, data }` |
| Actuator health | Done | `/actuator/health`; Redis health disabled by default for local optional Redis |

## Endpoints

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/refresh
POST /api/auth/logout
GET  /api/auth/me
POST /api/auth/change-password
POST /api/auth/re-auth
GET  /api/auth/sessions
POST /api/auth/sessions/{sessionCode}/revoke

GET/POST/PUT/PATCH/DELETE /api/vault/**
GET /actuator/health
```

## Configuration

```text
AUTH_SERVICE_URL=http://localhost:5001
VAULT_SERVICE_URL=http://localhost:5002
AUTH_JWT_SECRET=<same secret as auth-service>
GATEWAY_REDIS_BLACKLIST_ENABLED=true
GATEWAY_REDIS_FAILURE_BACKOFF_SECONDS=30
GATEWAY_CORS_ALLOWED_ORIGINS=*
```

## Verification

Manual verification used a temporary gateway on port `9010`:

```text
GET  /api/auth/me without token -> 401 GW-401
POST /api/auth/register -> 200
POST /api/auth/login -> 200
GET  /api/auth/me with access token -> 200
GET  /api/vault/health-check without token -> 401 GW-401
GET  /api/vault/health-check with token -> 503 GW-503 while vault-service is unavailable
```
