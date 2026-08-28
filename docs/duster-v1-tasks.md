# Duster v1 — Task Tracker

Rationale and architecture lives in `duster-v1-design.md`. The multi-phase arc (which Authos
capability gates which Duster tier) lives in `roadmap.md`. This file tracks status: what's
done, what's next. Update it whenever that changes — don't let it drift.

**Priority order:** Duster production-readiness first, then Authos improvements — but per
`roadmap.md`, Authos gaps that *block* a tier (PKCE, `/oauth/revoke`, introspection, device flow)
lead their phase.

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
- [x] `POST /internal/apps/create` is upsert-safe — re-sync no longer resets configured fields
      (`successUrl`/`logoutRedirectUrl`/`webhookSecret`/`sessionTtl`) back to defaults
- [x] Silent session refresh (`tryAccessTokenExchange`) + sliding Redis TTL
- [x] `SameSite=Strict` on the `duster_session` cookie
- [x] `k8s/ingress.yml` moved to `k8s/optional/` (opt-in, not applied by default)
- [x] `/authorize` request always sends `prompt=consent` when scope requires it (was the reported
      "duster requests offline_access without prompt=consent" bug - fix already in the tree,
      confirmed working end-to-end via live browser + curl reproduction, 2026-08-28)
- [x] Fixed `success_url` default (`"/"`) 404ing against Duster's own origin - not a Duster bug,
      but every existing test app had it unconfigured; documented as decision #20

### dstr-cli
- [x] `dstr apps configure` (success/logout url, webhook secret, session ttl)
- [x] Admin token plumbing: `--admin-token` / `DUSTER_ADMIN_TOKEN` / `~/.dstr/dstr.config`
- [x] Fixed `dstr credentials save` hitting the wrong URL (swapped path segments, always 404'd)
- [x] `dstr apps` (list + single) now shows `successUrl`/`logoutRedirectUrl`/`sessionTtl`/
      `webhookSecret` - `DusterAppDto` was missing all four fields entirely

### Authos
- [x] Consent screen (`ConsentForm.tsx`) renders the real client name, real requested scopes
      (including `offline_access`), and the real authenticated user's email via a new
      `GET /oauth/client-info` endpoint + the existing `GET /verify` — was 100% hardcoded
      placeholder data (`"test0"`, `["profile","email"]`, `"stefantoska@authos.com"`)
- [x] `/duster/pull` rejects cross-tenant pulls (`403`) instead of letting any Duster
      service-account token read any app's plaintext client secret by `client_id`
- [x] `GET /test/callback` reference `success_url` landing route (decision #20): resolves the
      `duster_session` cookie via Duster's `GET /session`, mints Authos's own login cookie
- [x] `POST /test/callback` webhook now actually returns decision #3's `200 {}` contract (was
      still returning a stale `302`, so Duster's `isSuccess()` check silently failed every time)

---

## Next Up

### Duster (current priority)
- [ ] Logout doesn't revoke the upstream refresh token — blocked on Authos `/oauth/revoke`
- [ ] `ApplicationTest.kt` needs a real `testApplication` harness (pre-existing, broken before
      this round of work — hits an unstarted external port, no `client_id`)

### Authos (next phase)
- [x] PKCE validation: `code_challenge` stored in the `ShortSession` at `/oauth/authorize` (S256
      only), `code_verifier` verified at `/oauth/token` in `handleAuthorizationCodeRequest`.
      "Verify if present" + RFC 7636 §4.6 downgrade protection. Pure-function `matchesS256Challenge`
      unit-tested against the RFC 7636 Appendix B vector (`PkceVerifierTest.kt` — repo's first real
      test). No DB migration. Branch `feature/authos-pkce-impl`.
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

- **2026-08-28 (2)** — Diagnosed the reported "duster requests offline_access without
  prompt=consent" / "login flow doesn't work" report by driving the real stack end-to-end
  (curl + headless browser against live authos-api/duster/frontend/Postgres/Redis, not just
  reading code). Findings: the `prompt=consent` fix was already written but never verified;
  the actual remaining breakage was the consent screen rendering fully hardcoded fake data.
  Also ran `dstr credentials save` + `dstr sync` for the first time ever against this stack
  (`~/.dstr/dstr.config` didn't exist) — surfaced two real bugs only visible from that path:
  `/internal/apps/create` wiping locally-configured app fields on every re-sync, and
  `/duster/pull` having no ownership check on the app it returns secrets for. Fixed all three
  (see decisions #18, #19) and reproduced+reverified each with the app reset back to clean
  state afterward. Local-dev note: the IntelliJ-launched `duster` run config had no
  `DUSTER_ADMIN_TOKEN` set (docker-compose already defaults it to `dev-admin-token`) — every
  `/internal/*` call was silently 401ing until it was added to that run's environment.

- **2026-08-28 (3)** — User hit a live `404` clicking the frontend's "Authos Login Test" button.
  Traced it to the existing-session short-circuit in `/oauth/start` redirecting to `success_url`,
  whose value was still the unconfigured default `"/"` - resolves against Duster's own origin,
  which has no route for it. Rather than just picking a URL, worked through what `success_url` is
  actually for (decision #4) and built the missing piece: a `GET /test/callback` handler in
  Authos that implements the developer-backend contract for real (see decision #20), plus fixed
  the sibling `POST /test/callback` to match decision #3's `200 {}` contract (it had silently been
  returning `302` this whole time). Also fixed `dstr apps` never displaying `successUrl` et al. -
  `DusterAppDto` was missing the fields; while there, corrected which fields need Authos's
  snake_case `@JsonProperty` mapping vs. Duster's bare-camelCase ones, verified against the actual
  wire response rather than assumed. Verified live: reused a real existing Duster session to hit
  the short-circuit path end-to-end (no synthetic fixtures), confirmed the new route mints a real
  Authos login cookie, and ran `dstr apps` / `dstr apps configure --success-url` against a live
  Duster with the admin token set to confirm the CLI fixes for real, not just by compiling clean.
