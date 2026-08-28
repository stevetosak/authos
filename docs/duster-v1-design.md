# Duster v1 — Design Decision Log

**Date:** 2026-06-14  
**Status:** Finalized, pre-implementation

---

## Context

Duster is a self-hosted OAuth2/OIDC BFF (Backend For Frontend) proxy that mediates between a developer's application and the Authos hosted IDP. The goal is to let developers delegate authentication entirely to Duster with minimal integration code, while Authos handles all OIDC complexity.

**Ecosystem:**
```
Browser
  ├─→ Developer's app (any stack)
  └─→ Duster (:8785, reverse-proxied behind developer's domain)
          └─→ Authos (hosted SaaS IDP)
```

---

## Decisions

### 1. Callback Payload Authentication
**Decision:** HMAC-SHA256 signed payload. Duster sends `X-Duster-Signature: sha256=<hex>` on every POST to `callbackUri`. Signature is computed over the raw JSON body using a per-app shared secret.

**Rationale:** Duster is publicly exposed (OAuth routes must be reachable by the browser). Network-level "host internally" is an advisory mitigation, not an enforceable guarantee. Signing makes the security model explicit and stack-agnostic.

---

### 2. Duster Network Exposure
**Decision:** Duster is publicly exposed. Its OAuth routes (`/start`, `/callback`) must be reachable by the browser. The developer's `callbackUri` backend endpoint should be internal (reachable by Duster, not the browser).

**Rationale:** The browser initiates the flow by hitting `/start` and receives the redirect back from `/callback`. There is no way to hide these routes. The distinction is: Duster OAuth routes = public, developer backend callback = internal.

---

### 3. Callback Response Contract
**Decision:** Developer's `callbackUri` returns `200 OK` with a JSON body. The response DTO is reserved for future extensibility — Duster reads and deserializes it but ignores unknown fields in v1.

```json
{}
```

Redirect destination is driven by `success_url` configured in dstr-cli, not by the callback response.

**Rationale:** The previous `Location` header contract was non-standard and broken — cookies set in the server-to-server callback response never reach the browser. JSON body is explicit and extensible.

---

### 4. Session Ownership
**Decision:** Duster owns the session entirely.

- After successful OIDC flow, Duster generates a `duster_session` UUID, stores userinfo + tokens in Redis
- Redis key: `duster:session:<clientId>:<uuid>`
- Duster sets `duster_session` as an `HttpOnly`, `Secure`, `SameSite=Strict` cookie on the browser
- Duster redirects browser to configured `success_url`
- Developer's protected routes call `GET /duster/api/v1/session?client_id=<id>` server-to-server with the session ID

**Rationale:** Eliminates all session storage requirements from the developer's side. Developer never writes auth state management code.

---

### 5. Deployment Topology
**Decision:** Duster runs behind a reverse proxy on the developer's own domain.

- nginx: `location /duster/ { proxy_pass http://duster:8785; }`
- The `duster_session` cookie is scoped to the developer's domain
- Developer's backend reads the cookie from incoming requests, forwards session ID to Duster server-to-server
- Duster is never directly exposed on its own domain

**Rationale:** Solves the cookie domain problem — cookie is same-origin with the developer's app. Duster stays internal. Developer adds ~3 lines to their existing nginx/proxy config.

---

### 6. Kubernetes Deployment
**Decision:** Duster deploys as a `ClusterIP` Service (internal only). Duster's own `k8s/ingress.yml` is removed or made opt-in. Developer adds a path rule to their existing Ingress:

```yaml
- path: /duster
  pathType: Prefix
  backend:
    service:
      name: duster
      port:
        number: 8785
```

Developer's backend calls Duster via cluster-internal DNS: `http://duster:8785`.

---

### 7. Multi-Tenancy
**Decision:** One Duster instance, N apps. Sessions are isolated by `clientId` in Redis key namespacing (`duster:session:<clientId>:<uuid>`). The `/session` endpoint requires `client_id` param and only reads sessions for that app.

---

### 8. Logout
**Decision:** Duster-owned, browser-initiated.

**Flow:**
1. Browser hits `GET /duster/api/v1/logout?client_id=<id>` (link or redirect from developer's app)
2. Duster deletes `duster:session:<clientId>:<uuid>` from Redis
3. Duster calls Authos token revocation endpoint with the stored refresh token
4. Duster sets `Set-Cookie: duster_session=; Max-Age=0` (clears cookie)
5. Duster redirects browser to configured `logout_redirect_url`

Developer writes zero logout code.

---

### 9. `callbackUri` Webhook
**Decision:** Optional, non-blocking, response body reserved for future use.

- Duster fires a signed POST to `callbackUri` after successful login (if configured)
- Failure (timeout, 5xx, network error) is logged but does not block the login
- Response body is deserialized to a known DTO; unknown fields are ignored in v1
- Use case: first-time user provisioning, syncing user to developer's own DB

**Future:** `webhook_required: true` flag in dstr-cli to gate login on webhook success for developers who need strict provisioning guarantees.

---

### 10. Backend SDKs
**Decision:** Ship thin middleware libraries for:
- **Node/Express** — `dusterAuth()` middleware
- **Spring Boot** — `DusterAuthFilter` / `@DusterProtected`
- **.NET** — `UseDusterAuth()` middleware

Each implements session verification as a one-liner. All make a server-to-server call to `GET /duster/api/v1/session?client_id=<id>` with the forwarded session cookie.

---

### 11. Frontend Library
**Decision:** Ship in order: React → Vue → vanilla JS.

**React API:**
```tsx
<DusterProvider clientId="..." onUnauthenticated="redirect">
  <ProtectedRoute>...</ProtectedRoute>
</DusterProvider>
```
```ts
const { user, login, logout } = useDuster()
```

- `login()` → redirects to `/duster/api/v1/oauth/start?client_id=<id>`
- `logout()` → redirects to `/duster/api/v1/logout?client_id=<id>`
- `user` → cached userinfo from `GET /duster/api/v1/session`

---

### 12. Frontend Library Cache Strategy
**Decision:** Cache userinfo in memory. Invalidate on `401` response from `/session`. Re-fetch triggers `onUnauthenticated` callback (default: redirect to login).

---

### 13. Developer Onboarding — `dstr init`
**Decision:** Interactive wizard command that handles full setup in one flow:

```
$ dstr init
? Authos account email: ...
? App name: my-app
? Success URL (where to redirect after login): https://myapp.com/dashboard
? Logout redirect URL: https://myapp.com/
? Backend callback URL (optional, for user provisioning): https://myapp.com/auth/hook

✓ Authenticated with Authos
✓ App created (client_id: abc123)
✓ Credentials saved to Duster
✓ Config synced

Add to your nginx config:
  location /duster/ { proxy_pass http://localhost:8785/; }

Add to your frontend:
  <a href="/duster/api/v1/oauth/start?client_id=abc123">Login</a>
```

---

### 14. CLI Authentication to Authos
**Decision:** Device Authorization Flow (RFC 8628) as the target. PAT (Personal Access Token from Authos dashboard) as the v1 fallback until Authos implements the device flow endpoint.

```
$ dstr init
Visit https://authos.io/device and enter code: ABCD-1234
Waiting... ✓ Authenticated
```

---

### 15. PKCE
**Decision:** Implemented in v1 in both Duster and Authos simultaneously.

**Duster changes:**
- `StateStore` generates and stores `code_verifier` alongside state
- `generateAuthorizeUrl()` appends `code_challenge=BASE64URL(SHA256(verifier))&code_challenge_method=S256`
- `/callback` retrieves verifier from `StateStore`, sends `code_verifier` in token exchange request

**Authos changes:**
- Validate `code_challenge` on `/oauth/authorize`
- Validate `code_verifier` on `/oauth/token`

---

### 16. Session TTL
**Decision:** Configurable via dstr-cli, default 24h. Silent refresh using stored refresh token extends the session without re-prompting the user. Session ends only on explicit logout or refresh token expiry.

```
$ dstr config set session_ttl 86400
```

---

### 17. Internal Management API Authentication
**Decision:** `/duster/api/v1/internal/*` (app registry, CLI service-account credentials) requires
`Authorization: Bearer <DUSTER_ADMIN_TOKEN>`, checked in constant time, on every request. An unset
token fails closed — every internal request is rejected until it's configured. Configured via the
`DUSTER_ADMIN_TOKEN` env var (server) and `--admin-token` / `DUSTER_ADMIN_TOKEN` / `~/.dstr/dstr.config`
(CLI).

**Rationale:** These routes return plaintext client secrets and can register or overwrite tenant
apps and the CLI's own service-account credentials. They share the public port with the OAuth
routes (decision #2), so network placement alone cannot be trusted to keep them internal — the
same reasoning decision #1 already applies to webhook signing.

---

## Status

This file records *why*. Current task status ("what's done, what's next") lives in
`duster-v1-tasks.md` — update that file, not this one, as work progresses.