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

### 18. Internal App Registration Is Upsert, Not Create-Only
**Decision:** `POST /duster/api/v1/internal/apps/create` must merge onto the existing `DusterApp`
by `client_id` (preserving `successUrl`, `logoutRedirectUrl`, `webhookSecret`, `sessionTtl`) when
one already exists, rather than always constructing a fresh record with field defaults.

**Rationale:** `dstr sync` posts to this same endpoint on every re-sync (e.g. after a client
secret rotation or app rename in Authos) — it is not a one-time registration call. Treating it as
create-only silently reset any `dstr apps configure` values back to their defaults on the next
sync, discarding real config with no warning. Found by actually running `dstr credentials save` +
`dstr sync` end-to-end for the first time (2026-08-28) — this path had never been exercised before.

---

### 19. `/duster/pull` Is Scoped To The Calling Duster Client's Owner
**Decision:** `POST /duster/pull` requires the presented Duster-service-account access token's
associated user to match the target app's owning user; mismatches get `403`. A `client_credentials`
token now carries its issuing `DusterApp`'s owner (`TokenService.generateAccessToken`'s
`clientCredentialsUser` param) so this can be checked — previously every `client_credentials` token
had `user = null` unconditionally.

**Rationale:** The endpoint returns a decrypted plaintext `client_secret`. Without an ownership
check, any developer holding valid Duster CLI credentials could pull — and thus impersonate — any
other tenant's app by guessing/enumerating its `client_id`, regardless of who registered it.


---

### 20. `success_url` Lands On The Developer's Backend, Not Duster Or A Config-Only URL
**Decision:** `success_url` must point at a real route on the developer's own backend that
implements decision #4's contract (read the `duster_session` cookie, call
`GET /duster/api/v1/session?client_id=<id>` server-to-server, establish the developer's own login
state). It is never the bare default (`"/"`, which resolves against Duster's own origin and 404s -
Duster has no UI) and never `callbackUri` (decision #2 requires that stay internal-only; it's also
`POST`-only and shaped for the webhook body, not a browser `GET`).

For `dusterTestApp` (Authos acting as its own "developer app" for testing), this reference
implementation lives at `GET /test/callback` in `DusterEndpoints.kt` - a new handler alongside the
existing `POST /test/callback` webhook, both on the same path, split by verb. The `POST` handler
was also fixed to actually return decision #3's `200 {}` contract; it had still been returning a
stale `302 Location` from before that decision, so `isSuccess()` was silently false on every login.

Also matters that this route is genuinely browser-facing, unlike the `POST` webhook: a missing or
expired `duster_session` cookie, or a failed Duster session lookup, must not surface as a raw 500.
Both map to a `302` to `$frontendHost/error`, reusing `ExceptionHandler.kt`'s existing fallback
convention (`redirectUri ?: "$frontendHost/error"`) rather than throwing.

**Rationale:** Found by walking through what "the success url should point at the backend" would
actually require end-to-end (2026-08-28) rather than picking an arbitrary URL. `success_url`'s
whole job per decision #4 is landing the browser somewhere that can exchange the now-set cookie for
identity - that only works if something is listening that implements that exchange.

---

## Adaptability Review (2026-08-28)

Decisions #21–30 come from walking the v1 flow — finalized above for a *single* app shape (owns a
domain, owns a backend, can reverse-proxy `/duster`, can add session-check middleware) — against
the other shapes Duster is meant to serve and against the stated goal of letting an app delegate
auth entirely with **no code**. Where they conflict with an earlier decision they say so
explicitly: #22 carves an exception into #20, #23 reverses the cookie choice in #4.

---

### 21. Integration Is A Named Tier Ladder; v1 Ships Tiers 0–2
**Decision:** An integrating app picks the lowest tier that covers its needs, rather than following
one implicit path.

| Tier | Name | App provides | Duster provides the rest |
|------|------|--------------|--------------------------|
| 0 | Frontend-only (zero-code) | reverse-proxy `/duster`, a login link, one JS snippet | session, identity, silent refresh, logout |
| 1 | Frontend-only, cross-origin | tier 0 without the same-origin proxy; registers `allowed_origins` | tier 0 + CORS + `SameSite=None` cookie |
| 2 | Backend BFF *(the #4 / #20 model)* | a backend, the `success_url` landing route, session-check middleware | server-to-server `/session`, keeps its own login state |
| 3 | Token-forwarding BFF | tier 2 + declares downstream APIs | proxies outbound calls with the user's access token attached |
| 4 | Native / device | a public client, secure token storage | device-flow auth, opaque session token (no cookie) |

v1 scope is tiers 0–2. Tiers 3–4 are deferred (#30) but the session model must not be built in a
way that forecloses them.

**Rationale:** Decisions #1–20 collectively assume tier 2 and make it the only door in — `success_url`
must be a backend route (#20), `/session` is server-to-server (#4), there is no browser-facing
identity endpoint and (`HTTP.kt`) no CORS layer at all. That is the opposite of "delegate auth to
Duster without writing any code." Naming the tiers turns "unsupported" into "a lower tier" and makes
each decision below a scoped change instead of a redesign. Tiers 0–2 share nearly all existing code;
the deltas are cookie attributes, one endpoint, and a per-app CORS allowlist.

---

### 22. Tier 0 — Zero-Code Frontend-Only Integration
**Decision:** An app with no backend integrates entirely from the browser:

- **Login:** `<a href="/duster/api/v1/oauth/start?client_id=<id>">`.
- **Identity:** the SPA calls `GET /duster/api/v1/me` on load — reads the `duster_session` cookie,
  returns pruned userinfo, `401` if absent/expired. Behaviourally identical to `/session` (#4); the
  separate name exists so `/me` can be documented as browser-facing and `/session` as
  server-to-server. One handler may back both.
- **`success_url` may be a plain SPA route** (e.g. `/`) at this tier — the carve-out #20 said it
  would need. The browser lands there, the SPA calls `/me`, done.
- **Logout:** `<a href="/duster/api/v1/logout?client_id=<id>">` (see #26).
- The decision #11 frontend library ships pointed at `/me` with no backend assumed.

**Requirement:** `/duster` is reverse-proxied onto the SPA's own origin (#5). Hosts that can rewrite
(Netlify, Vercel, Cloudflare, nginx) qualify; static hosting that cannot proxy is tier 1.

**Rationale:** The objection that sank "redirect straight to the frontend" (the hop discussion,
2026-08-28) was specifically about redirecting to a *different-origin* frontend under
`SameSite=Strict`. When the SPA is same-origin with the proxied `/duster`, the
`myapp.com/duster/callback → myapp.com/` redirect is same-site, the cookie rides along, and the
`/me` fetch is same-origin — nothing has to "exchange" the cookie. The backend hop in #20 exists
only to mint *the app's own* credential; a tier-0 app has none — Duster's session **is** the
credential — so the hop has nothing to do.

---

### 23. `duster_session` Is `SameSite=Lax`, Not `Strict`
**Decision:** Change the `duster_session` cookie from `SameSite=Strict` to `SameSite=Lax` in both
`OAuthRoutes.kt` and `SessionRoutes.kt`.

**Rationale:** `Strict` withholds the cookie on the first cross-site top-level navigation into the
app — a user following a link to `myapp.com/dashboard` from an email or another site gets an
unauthenticated first paint. That is broken for SSR apps and merely invisible for SPAs (the `/me`
fetch recovers it). `Strict` was chosen defensively but buys almost nothing here: `/session` and
`/me` are reads, and `/logout` moves to `POST` + CSRF token for the tiers that expose it to the
browser (#27). `Lax` is the conventional session-cookie choice and fixes deep-linking.

---

### 24. `duster_session` Cookie Is Client-Scoped
**Decision:** The cookie name carries the client — `duster_session_<clientId>` (or
`ds_<short-hash(clientId)>` to bound the length). `/start`, `/callback`, `/session`, `/me`, and
`/logout` all read the client-suffixed name.

**Rationale:** Today the cookie is the bare literal `duster_session`, `path=/`. Two Duster-backed
apps proxied under one domain — `/duster` shared behind one nginx — collide on that name, and app
B's login **overwrites** app A's session cookie in the browser. Decision #7's multi-tenancy only
holds across *distinct* domains; tier 0 makes co-hosting common (small apps that don't each own a
domain), so the cookie has to be per-client too.

---

### 25. Token Storage Keys Are Client-Scoped
**Decision:** Token Redis keys move from the global `<type>_token:sub:<sub>` to
`duster:token:<clientId>:<sub>:<type>`. `TokenRepository.saveAll` / `getToken` take a `clientId`.

**Rationale:** Sessions are keyed `duster:session:<clientId>:<uuid>`, but tokens are keyed by `sub`
alone. Authos issues a *pairwise* `sub` per AppGroup (PPID), so two Duster apps in the **same
AppGroup** resolve the same `sub` for a user — and the second login's `saveAll` clobbers the
first's refresh token (`refresh_token:sub:<sub>`, stored with no TTL). The session layer believes
the apps are isolated; the token layer silently is not. Client-scoping the keys realigns them.

---

### 26. `/logout` Revokes Upstream And Purges Tokens
**Decision:** `/logout` performs, in order: (1) call Authos token revocation with the stored refresh
token, (2) delete the `duster:token:<clientId>:<sub>:*` keys, (3) delete
`duster:session:<clientId>:<uuid>`, (4) clear the cookie, (5) redirect to `logout_redirect_url`.
Step (1) is decision #8's revocation step, which was never implemented — **the revocation endpoint
does not exist in Authos yet and has to be built** before this decision can fully land; until then
Duster does (2)–(5) and the refresh token is orphaned rather than invalidated.

**Rationale:** Current `/logout` does only (3)–(5). The access / id tokens then sit in Redis until
their TTL and the refresh token (no TTL) lives forever — and stays *valid* at Authos, because
nothing revokes it. A logout that leaves a usable refresh token server-side is not a logout.

---

### 27. Tier 1 — Cross-Origin Frontend Via Per-App `allowed_origins`
**Decision:** `DusterApp` gains `allowedOrigins: List<String>`, set via `dstr apps configure`. When
non-empty, for that app only:

- Duster enables CORS for exactly those origins with `Access-Control-Allow-Credentials: true`.
  There is currently no CORS plugin installed (`HTTP.kt`), so this is net-new.
- `duster_session` is issued `SameSite=None; Secure` — required for the browser to send it on
  cross-site XHR to `/me`.
- `/logout` requires `POST` + a CSRF token (double-submit cookie, or an `X-Duster-Csrf` header
  echoed from `/me`), since `SameSite=None` removes the ambient protection that made the
  `GET /logout` link safe at tiers 0/2.

**Rationale:** Unlocks the shapes tier 0 cannot reach: SPAs on static hosts that cannot
reverse-proxy, and SaaS where the frontend and API sit on different registrable domains. Gating it
on an explicit per-app allowlist keeps the default tight — `SameSite=Lax`, no CORS, cookie locked
to the setting origin — and makes cross-origin credentialed cookies opt-in per app rather than a
global posture.

---

### 28. Duster's Own `/callback` Failures Redirect, Not `500`
**Decision:** The catch block in `OAuthRoutes.kt` `/callback` redirects the browser to a
configurable `error_url` (default: the `success_url` origin + `/error`) — the convention #20 applied
to the backend landing route. `DusterApp` gains `errorUrl`.

**Rationale:** State-validation failure, Authos unreachable, or a failed code exchange currently
produces `respond(500, {"error": "Authentication failed"})`, and the `StatusPages` handler in
`Routing.kt` renders any uncaught throwable as the literal string `500: <exception>`. A browser is
on the other end of `/callback`; it should land on a real page. #20 fixed this for the developer's
landing route but not for Duster's own callback, which is a superset of the failure modes.

---

### 29. Webhook Delivery Is Retried, With A Dead-Letter Fallback And An Optional Gate
**Decision:**
- POSTs to `callbackUri` retry on failure with bounded exponential backoff (≈3 attempts over ~15s).
- After the final failed attempt the payload lands on a Redis list `duster:webhook:dlq:<clientId>`
  for inspection and manual replay.
- `DusterApp` gains `webhookRequired: Boolean` (decision #9's "future" flag). When `true`, a webhook
  that never succeeds fails the login — redirect to `error_url` (#28) — instead of being swallowed.

**Rationale:** Delivery today is `CoroutineScope(Dispatchers.IO).launch { … }` fire-and-forget with
a `println` on failure. Fine for "nice-to-have analytics"; unusable for #9's actual purpose —
first-time user provisioning — where a dropped webhook means a user who logged in but does not exist
in the app's own database. Retry + DLQ + an opt-in hard gate serves both postures without forcing
the strict one on everyone.

---

### 30. Tiers 3–4 Are Deferred, With Their Constraints Recorded Now
**Decision:** Token-forwarding (tier 3) and native / device (tier 4) are out of scope for v1. To
keep them reachable:

- **Do not** assume the `duster_session` value is only ever delivered as a `Set-Cookie`. Tier 4
  needs Duster to return an opaque session token in a JSON body from a token endpoint and accept it
  as a bearer on `/me`. Keep session lookup independent of the cookie as a transport.
- **Do not** treat userinfo as the only thing a session can yield. Tier 3 adds an endpoint that
  proxies an outbound request to a declared downstream API with `duster:token:<clientId>:<sub>:access`
  attached — the tokens are already stored (#25); only the egress path is missing.

**Rationale:** Both are genuine Duster use cases — an app calling resource APIs on the user's
behalf, a mobile client — that need more than a cookie-attribute change. Recording the constraints
now is cheap insurance against a v1 decision that quietly closes the door on them.

---

## Status

This file records *why*. Current task status ("what's done, what's next") lives in
`duster-v1-tasks.md` — update that file, not this one, as work progresses.