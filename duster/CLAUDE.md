# CLAUDE.md — Duster

This file provides guidance to Claude Code when working with this repository.

## Project Overview

Duster is a **Ktor-based Kotlin OAuth2/OIDC BFF (Backend For Frontend) proxy** that mediates between client applications and the [Authos](../authos) identity provider. It handles OAuth authorization flows, token storage, and automatic token refresh on behalf of registered apps.

- **Port:** 8785
- **Framework:** Ktor 3.1.3 (Netty), Koin 4.1.0 (DI), Lettuce 6.7.1 (Redis)
- **Build:** Gradle with Kotlin DSL

### Integration tiers (design doc decisions #21–28)

An app picks the lowest tier that covers it. v1 ships tiers 0–2.

| Tier | Shape | Duster deltas |
|------|-------|---------------|
| 0 | Frontend-only, `/duster` reverse-proxied onto the SPA's own origin | `duster_session_<clientId>` cookie `SameSite=Lax`, `GET /me` + `GET /logout` links, `success_url` may be a plain SPA route |
| 1 | Frontend-only, **cross-origin** (static host, or FE/API on different domains) | app registers `allowed_origins` → per-app credentialed CORS + cookie `SameSite=None; Secure` + `POST /logout` with an `X-Duster-Csrf` token |
| 2 | Backend BFF (the original model) | server-to-server `GET /session`, app keeps its own login state |

The default posture (empty `allowed_origins`) is tier 0/2: no CORS headers, `SameSite=Lax`, `GET /logout` works.

## Build & Run

```bash
# Run locally (requires Redis and Authos IDP)
./gradlew run

# Build fat JAR
./gradlew buildFatJar          # outputs build/libs/fat.jar

# Build Docker image
./gradlew buildImage

# Run tests
./gradlew test
```

## Environment Variables

All configuration is via environment variables. No value is required in production except those marked *.
Full deployment guide (every target, provisioning): `DEPLOY.md`.

| Variable | Default | Description |
|----------|---------|-------------|
| `AUTHOS_BASE_URL`* | `http://<HOST_IP>:8080` | Base URL of the Authos IDP. **Must byte-match the Authos discovery `issuer`** — `JwtHandlers.kt` checks the ID token `iss` against it and loads JWKS from it. |
| `PORT` | `8785` | Listen port (`Application.kt`). |
| `REDIS_URL` | _(none)_ | `redis[s]://[:password@]host:port[/db]` — one var for any managed/TLS Redis. **Wins over the discrete `REDIS_*` below** (`RedisConfig.redisUri()`). |
| `REDIS_HOST`* | `localhost` | Redis hostname (ignored if `REDIS_URL` set) |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_PASSWORD` | _(none)_ | Redis auth password — URL-encoded into the URI for you |
| `REDIS_USESSL` | `false` | `true` ⇒ `rediss://` |
| `REDIS_TIMEOUT` | `60` | Redis connection timeout (seconds) |
| `HOST_IP` | `localhost` | Rewrites `localhost` in a webhook callback URI for Docker/VM setups. Irrelevant to tier 0. |
| `DUSTER_ADMIN_TOKEN`* | _(none — unset fails closed)_ | Bearer secret required on every `/duster/api/v1/internal/*` request (app registry, CLI credentials). Generate with `openssl rand -hex 32`. Must match `dstr`'s `--admin-token` / `DUSTER_ADMIN_TOKEN` / `~/.dstr/dstr.config`. |

## Internal Management API

`/duster/api/v1/internal/*` (app registration/listing/config, CLI service-account credentials) returns
plaintext client secrets and lets callers register/overwrite tenant apps. It shares the same public
port as the OAuth routes, so it is gated by `DUSTER_ADMIN_TOKEN` (`security/AdminAuth.kt`) rather than
relying on network placement alone — the same "don't trust the network boundary" rationale the design
doc applies to webhook HMAC signing (`docs/duster-v1-design.md`, decision #1). An unset token fails
closed: every internal request is rejected until it's configured.

`PATCH /internal/apps/config?client_id=<id>` sets per-app config (all optional, merged onto the
stored app): `success_url`, `logout_redirect_url`, `error_url` (root-relative path or absolute
http(s); `""` resets `error_url` to the derived default), `webhook_secret`, `session_ttl`,
`allowed_origins` (list of bare origins — `https://app.example.com`, no path/trailing slash).
`POST /internal/apps/create` is upsert-safe: a re-sync from `dstr` preserves every field above.

## Running with Docker

**Local dev (bundled Redis):**
```bash
docker compose up
```

**External Redis:**
```bash
# Set your Redis connection in env or .env file, then:
docker compose -f docker-compose.yml -f docker-compose.external-redis.yml up
```

**Production:**
```bash
# Set all required env vars in a .env file, then:
docker compose -f docker-compose.prod.yml up
```

## Kubernetes Deployment

`k8s/` is a Kustomize **base** (`k8s/base/`) + an example **overlay** (`k8s/overlays/example/`).
The base is namespace-agnostic; the overlay supplies namespace, image tag, and the `duster-config`
ConfigMap + `duster-secrets` Secret. Full walkthrough (all targets, provisioning) in `DEPLOY.md`.

```bash
# copy the example overlay, edit values, then:
kubectl apply -k k8s/overlays/example
```

`livenessProbe` → `/health/live` (process only — a Redis blip never restarts the pod),
`readinessProbe` → `/health` (pings Redis). `k8s/optional/ingress.yml` is a **tier-1** reference
(Duster on its own host); **tier 0** adds a `/duster` path rule (no rewrite) to the consuming app's
own Ingress instead (design decision #6).

## Architecture

```
Client App → Duster (:8785) → Authos IDP (AUTHOS_BASE_URL)
                ↓
             Redis (token store)
```

**Key code paths:**
- Entry: `Application.kt` → `mainModule()` → `Routing.kt` → route handlers
- OAuth flow: `routes/OAuthRoutes.kt` `/start` → `DusterRequestService` → `DusterOAuthClient` → Authos → `/callback` → verify JWT → store tokens + session in Redis, set `duster_session_<clientId>` cookie
- `/callback` failure: catch block redirects to the app's `error_url` (default: `success_url` origin + `/error`) — never a raw 500 (`errorRedirectTarget()` in `Utils.kt`)
- Session read: `routes/SessionRoutes.kt` `readSession()` backs both `GET /session` (server-to-server) and `GET /me` (browser-facing); silent-refreshes the access token on every call, returns `X-Duster-Csrf` on success
- Logout: `handleLogout()` (GET **and** POST) → revoke the grant at Authos (`POST /oauth/revoke`, best-effort) → purge `duster:token:<clientId>:<sub>:*` → drop the session → clear the cookie → redirect to `logout_redirect_url`. Tier-1 apps (`allowed_origins` set) must `POST` + send the `X-Duster-Csrf` header from `/me`
- Token refresh: `DusterRequestService.tryAccessTokenExchange()` → Redis lookup (keyed by `clientId` + `sub`) → `DusterOAuthClient.refreshTokenRequest()` on miss
- CORS: `routes/Cors.kt` — `corsPreflightRoutes()` (an `OPTIONS /duster/api/v1/{...}` handler) + `ApplicationCall.applyPerAppCors(app)`. Credentialed headers are echoed only for an `Origin` in that app's `allowed_origins`; wired in `Routing.kt`

**URL resolution** (`Utils.kt`):
- `getAuthosBaseUrl()` — reads `AUTHOS_BASE_URL` env var, falls back to `http://<HOST_IP>:8080`
- `getHostIp()` — reads `HOST_IP` env var, falls back to `localhost`

## Key Files

| File | Purpose |
|------|---------|
| `Application.kt` | Entry point, starts Netty on port 8785 |
| `Routing.kt` | Wires all route groups |
| `Modules.kt` | Koin DI module |
| `Utils.kt` | `getHostIp()` / `getAuthosBaseUrl()`, `dusterSessionCookieName()`, `isValidRedirectTarget()` / `safeRedirectTarget()`, `isValidOrigin()`, `sessionCookieSameSite()`, `errorRedirectTarget()` |
| `config/RedisConfig.kt` | Reads Redis config from env/HOCON |
| `config/UrlConfig.kt` | `getAuthosAuthorizeUrl()` |
| `service/DusterOAuthClient.kt` | HTTP calls to Authos (token, userinfo, callback webhook, `revokeRefreshToken`) |
| `service/DusterRequestService.kt` | Orchestrates token exchange and authorize URL generation |
| `service/RedisManager.kt` | Lettuce connection pool |
| `service/JwtHandlers.kt` | JWT signature verification via JWKS |
| `routes/OAuthRoutes.kt` | `/duster/api/v1/oauth/start` and `/callback` |
| `routes/SessionRoutes.kt` | `/session`, `/me`, `/logout` (GET+POST); `readSession()` + `handleLogout()` |
| `routes/Cors.kt` | Per-app CORS — `OPTIONS` preflight + `applyPerAppCors()` |
| `routes/InternalApiRoutes.kt` | App registration/listing/config and CLI credentials endpoints |
| `security/AdminAuth.kt` | `DUSTER_ADMIN_TOKEN` bearer check for internal routes |
| `routes/HealthRoutes.kt` | `GET /health/live` (process only — k8s liveness) + `GET /health` (pings Redis — k8s readiness) |

## Redis Key Patterns

| Key | Contents |
|-----|---------|
| `duster:token:<clientId>:<sub>:id` | ID token (TTL = token exp) |
| `duster:token:<clientId>:<sub>:access` | Access token (TTL = `expires_in`) |
| `duster:token:<clientId>:<sub>:refresh` | Refresh token (no TTL) |
| `duster:app:id:<clientId>` | App config |
| `duster:app:names` | Hash of name → clientId |
| `duster:credentials` | CLI service account credentials |
| `duster:session:<clientId>:<sessionId>` | Duster-owned session (sub + pruned userinfo + `csrfToken`), TTL = `app.sessionTtl`, slides on each `/session` / `/me` check |
| `duster:state:<stateId>` | PKCE `code_verifier` + `clientId` for an in-flight authorize request (TTL 300s) |

Token keys are scoped by `clientId`, not just `sub` (design decision #25): Authos issues a
pairwise (PPID) `sub` per AppGroup, so two Duster apps in the same group would otherwise share —
and clobber — one token slot. `TokenRepository.key(clientId, sub, type)` is the single source of
the scheme; `OAuthTokenRepository` methods all take `clientId` first.

## Local Development Prerequisites

1. **Authos IDP** running (default: `http://localhost:8080`)
2. **Redis** running (default: `localhost:6379`)

```bash
# Quick Redis via Docker:
docker run -d -p 6379:6379 redis:alpine
```
