# Duster v1 — Task Tracker

Rationale and architecture lives in `duster-v1-design.md`. This file tracks status: what's
done, what's next. Update it whenever that changes — don't let it drift.

**Priority order:** Duster production-readiness first, then Authos improvements.

---

## Done

### Duster
- [x] `duster_session` cookie + Redis-backed sessions, isolated by `clientId`
- [x] `GET /session`, `GET /logout` endpoints
- [x] PKCE on the Duster side: `StateStore` code_verifier/challenge, authorize URL, token exchange
- [x] HMAC-SHA256 signing on the callback webhook (`X-Duster-Signature`)
- [x] JSON body callback contract, response deserialized into `CallbackResponse`
- [x] `success_url` / `logout_redirect_url` / `webhook_secret` / `session_ttl` on `DusterApp`
- [x] `DUSTER_ADMIN_TOKEN` auth gate on `/internal/*` (was unauthenticated — fixed)
- [x] `PATCH /internal/apps/config` to actually set the fields above
- [x] Silent session refresh (`tryAccessTokenExchange`) + sliding Redis TTL
- [x] `SameSite=Strict` on the `duster_session` cookie
- [x] `k8s/ingress.yml` moved to `k8s/optional/` (opt-in, not applied by default)

### dstr-cli
- [x] `dstr apps configure` (success/logout url, webhook secret, session ttl)
- [x] Admin token plumbing: `--admin-token` / `DUSTER_ADMIN_TOKEN` / `~/.dstr/dstr.config`
- [x] Fixed `dstr credentials save` hitting the wrong URL (swapped path segments, always 404'd)

---

## Next Up

### Duster (current priority)
- [ ] Logout doesn't revoke the upstream refresh token — blocked on Authos `/oauth/revoke`
- [ ] `ApplicationTest.kt` needs a real `testApplication` harness (pre-existing, broken before
      this round of work — hits an unstarted external port, no `client_id`)

### Authos (next phase)
- [ ] PKCE validation: `code_challenge` on `/oauth/authorize`, `code_verifier` on `/oauth/token`
      (`GrantType.PKCE -> TODO()` today — Duster already sends both, Authos ignores them)
- [ ] Token revocation endpoint (`/oauth/revoke`)
- [ ] Device Authorization Flow (RFC 8628)

### dstr-cli (later)
- [ ] `dstr init` interactive wizard
- [ ] Device flow authentication (`dstr auth login`)

### New repos (future)
- [ ] `duster-node`, `duster-spring`, `duster-dotnet` — backend middleware
- [ ] `duster-react`, `duster-vue`, `duster-js` — frontend SDKs

---

## Log

- **2026-08-28** — Committed the session/PKCE/HMAC work-in-progress. Closed the internal-API
  auth gap (`DUSTER_ADMIN_TOKEN`), wired app config end-to-end, added silent session refresh,
  `SameSite=Strict` cookie, opt-in k8s ingress. Commits: `duster: session ownership, PKCE, HMAC
  webhooks`, `duster: gate internal API, wire app config, refresh sessions` on
  `feat/duster-auth-flow-refactor`.
