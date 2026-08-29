# `duster-v1-design.md` — archived decisions

Decisions that are fully shipped and settled, moved here from `duster-v1-design.md` to keep that
file under its line limit (see `docs/README.md` § Archiving). Numbers and headings are preserved
verbatim so cross-references (`#N`) and anchor links still resolve. Newest additions at the end.

---

### 4. Session Ownership
**Decision:** Duster owns the session entirely.

- After successful OIDC flow, Duster generates a `duster_session` UUID, stores userinfo + tokens in Redis
- Redis key: `duster:session:<clientId>:<uuid>`
- Duster sets `duster_session` as an `HttpOnly`, `Secure`, `SameSite=Strict` cookie on the browser
- Duster redirects browser to configured `success_url`
- Developer's protected routes call `GET /duster/api/v1/session?client_id=<id>` server-to-server with the session ID

**Rationale:** Eliminates all session storage requirements from the developer's side. Developer never writes auth state management code.

**Superseded in part:** `SameSite=Strict` → `Lax` (#23); the cookie name is client-scoped
`duster_session_<clientId>` (#24); `/me` is the browser-facing read alongside server-to-server
`/session` (#22).

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

**Status correction (2026-08-28):** only the Duster side was actually built in v1. Authos accepted
`code_challenge` / `code_verifier` as query params and silently ignored them until the PKCE
implementation landed (see `roadmap.md` Phase 0). The Authos side now: stores `code_challenge` +
`code_challenge_method` in the `ShortSession` at `/oauth/authorize` (S256 only — `plain` and a
missing method are rejected), and verifies `code_verifier` at `/oauth/token` inside
`handleAuthorizationCodeRequest` with "verify if present" enforcement plus RFC 7636 §4.6 downgrade
protection (verifier-without-challenge and challenge-without-verifier both rejected). No DB
migration — the challenge lives in Redis with the rest of the `ShortSession`.

---
