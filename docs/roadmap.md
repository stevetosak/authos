# Authos + Duster — Roadmap

**Date:** 2026-08-28
**Companion docs:** `duster-v1-design.md` (why — decision log), `duster-v1-tasks.md` (current-sprint
status), `automation-tests-plan.md` (test plan).

---

## Premise

Duster is a thin BFF. Every capability it offers is a re-packaging of something the **Authos API**
does — `authorize`, `token`, `refresh`, `userinfo`, and (not yet) `revoke` / `introspect`. A Duster
feature can only be as robust as the Authos endpoint beneath it.

So the integration-tier ladder from `duster-v1-design.md` #21 has a hard dependency spine on the
Authos side:

| Duster tier | Authos capability it needs | Authos status today |
|---|---|---|
| **0** — zero-code frontend | auth code + refresh + `prompt=none`; **real** `expires_in`; PKCE | auth code ✅ · refresh ✅ (30-day, no rotation) · `prompt=none` ✅ · `expires_in` ❌ hardcoded `3600` · **PKCE ❌ not implemented** |
| **1** — cross-origin frontend | same as tier 0 | same |
| **2** — backend BFF *(current model)* | + token revocation (RFC 7009); `end_session_endpoint` | revocation ❌ · end-session ❌ |
| **3** — token-forwarding BFF | + token introspection (RFC 7662); resource-server `aud`; generic `client_credentials` | introspection ❌ (bespoke `/duster/validate-token` stub) · `client_credentials` is Duster-only |
| **4** — native / device | + Device Authorization Flow (RFC 8628); public clients (PKCE, no secret) | `GrantType.DEVICE_CODE -> TODO()` · no public-client support |
| *all tiers* | discovery doc; custom/authz claims; JWKS rotation; audit + rate limiting | discovery ❌ · claims = standard OIDC only · single JWKS key · `println` logging |

This refines the "Duster production-readiness first, then Authos" ordering in `duster-v1-tasks.md`:
Duster-first still holds **within** a phase, but the Authos gaps that *block* a tier (PKCE,
revocation, introspection, device flow) now lead their phase.

---

## Phase 0 — Authos OIDC core hardening

*Blocks every tier. Nothing else should land on top of the current core.*

**Authos**
- **PKCE, end to end.** Add `code_challenge` + `code_challenge_method` to `ShortSession` at
  `/oauth/authorize`; accept and verify `code_verifier` (`S256`) on `/oauth/token`. It's a modifier
  on `authorization_code`, not the stubbed `GrantType.PKCE`. Duster already sends both params
  (`DusterOAuthClient.codeExchange`) and Authos silently ignores them — `duster-v1-design.md` #15
  over-claims this is done.
- **`GET /.well-known/openid-configuration`** — issuer, endpoint URLs, supported
  grants/scopes/claims, `jwks_uri`. Stops Duster and every SDK from hardcoding paths.
- **Real `expires_in`.** `/oauth/token` returns a hardcoded `3600`. Duster stores the token in Redis
  with exactly this value (`TokenRepository.saveAll`), so a wrong number means premature or stale
  silent refresh.
- **Consistent OAuth errors** — RFC 6749 §5.2 JSON on `/token`, redirect-with-`error` on
  `/authorize`. Some paths currently fall through `ExceptionHandler` to a raw 500, which Duster #28
  can't turn into a clean user-facing error.
- **Re-enable the `/approve` request-integrity check.** The parameter-hash comparison in
  `OAuthEndpoints.approve` is commented out (`"direktno ako go pristapis ova mozda e slabost"`).
  Tampering with params between `/authorize` and `/approve` is unchecked today.

**Exit criteria:** OIDC smoke suite passes (authorize → PKCE token → userinfo, `prompt=none`, error
cases); discovery doc validates; the existing Duster flow works unchanged against the hardened core.

---

## Phase 1 — Duster tiers 0 & 1 (zero-code) + revocation

**Authos**
- **`POST /oauth/revoke`** (RFC 7009) — revoke refresh + access token, set the existing
  `RefreshToken.revoked` flag (nothing sets it today). Unblocks the item already in
  `duster-v1-tasks.md` → Next Up.
- Verify `prompt=none` silent re-auth when the Authos SSO session outlives the Duster session —
  Duster's `/session` silent refresh (#16) depends on it.

**Duster** (design decisions #22–28)
- `GET /duster/api/v1/me` — browser-facing session read (#22)
- `success_url` may be a plain SPA route at tier 0 (#22)
- `SameSite=Strict` → `Lax` (#23)
- Client-scoped cookie name (#24)
- Client-scoped token Redis keys (#25) — fixes the same-AppGroup refresh-token clobber
- `/logout` calls `/oauth/revoke` + purges token keys (#26)
- Per-app `allowed_origins` → CORS + `SameSite=None` for tier 1 (#27)
- `/callback` failures redirect to `error_url`, not 500 (#28)

**SDKs**
- `duster-react` (then `-vue`, `-js`) pointed at `/me` — no backend assumed (`duster-v1-design.md` #11)

**Exit criteria:** a static-hosted SPA with only a proxy rule + the React snippet can log in, survive
silent refresh, and log out with the upstream token actually revoked.

---

## Phase 2 — Duster tier 2 hardening + headless onboarding

**Authos**
- **Machine client-registration API** — token-authenticated `POST /oauth/clients` (or an extension
  of `/duster/*`). `ApplicationController`'s `/app/*` endpoints are dashboard-session only, so
  `dstr init` can't create an Authos app headlessly.
- **`end_session_endpoint`** (OIDC RP-Initiated Logout) + SSO-group logout propagation — logging out
  of one app in an AppGroup ends the shared SSO session.
- **Refresh-token rotation + reuse detection** — rotate the value on every `refresh_token` grant,
  flag the token family on reuse of a retired value. Needed before Duster sessions (which refresh on
  every `/session` check) are safe to run for days.

**Duster**
- Webhook retry + `duster:webhook:dlq:<clientId>` + `webhook_required` gate (#29)

**dstr-cli**
- `dstr init` interactive wizard (#13) — now possible against the registration API

**Exit criteria:** `dstr init` takes a developer from zero to a working tier-2 integration in one
command; provisioning webhooks survive a flaky backend.

---

## Phase 3 — Duster tier 3 (token-forwarding)

**Authos**
- **`POST /oauth/introspect`** (RFC 7662) — replaces the bespoke `/duster/validate-token` stub.
- **Resource-server audiences** — issue access tokens with an `aud` for a named downstream API;
  `/oauth/userinfo` and introspection validate it.
- Generalize `client_credentials` beyond `dusterAppService.validateAppCredentials` so non-Duster
  M2M clients work.

**Duster**
- Forwarding proxy endpoint — Duster attaches `duster:token:<clientId>:<sub>:access` to an outbound
  call to a declared downstream API (`duster-v1-design.md` #30).

**Exit criteria:** a tier-2 app calls a separate resource API through Duster without ever handling a
token.

---

## Phase 4 — Duster tier 4 (native / device)

**Authos**
- **Device Authorization Flow** (RFC 8628) — `/oauth/device_authorization` + `device_code` grant
  (`GrantType.DEVICE_CODE -> TODO()`). Also unblocks `dstr auth login` (#14), currently on a PAT
  fallback.
- **Public clients** — PKCE-only token exchange with no `client_secret`.

**Duster**
- Opaque session-token endpoint + bearer acceptance on `/me` (no cookie) — the transport
  independence #30 asks the session layer to keep.

**Exit criteria:** a CLI or mobile client authenticates via device flow and holds a Duster session
with no browser cookie.

---

## Cross-cutting (every phase)

- **Audit logging** — replace `println` on the Authos auth paths with structured, queryable events
  (login, consent, token issue, revoke).
- **Rate limiting / brute-force protection** on `/oauth-login`, `/oauth/token`, `/register`.
- **JWKS rotation strategy** — publish overlapping `kid`s before rotating the signing key; Duster
  and every SDK cache JWKS and break on a hard swap.
- **Custom / authorization claims** — map roles / groups / entitlements into the ID token + userinfo
  so real apps at any tier get authz data, not just profile.
- **Test coverage** — the OAuth flows have no working harness (`ApplicationTest.kt` is broken; see
  `automation-tests-plan.md`). Each phase adds flow tests before it closes.

---

## Relationship to `duster-v1-tasks.md`

This file is the **multi-phase arc**. `duster-v1-tasks.md` is the **current-sprint tracker** — when
a phase goes active, its items move into that file's *Next Up* with checkboxes. Update the arc here
when priorities or dependencies shift; update the tracker there as individual items land.
