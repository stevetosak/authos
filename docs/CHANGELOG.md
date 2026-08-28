# Changelog

Dated, one-line-per-change history of what has landed on `master`. Newest entries at the **end**.

**Editing rule:** only ever *append* a new line (or a small block) at the end. Never reword,
reorder, or delete an existing entry. `.gitattributes` marks this file `merge=union`, so parallel
branches that each append their own line merge cleanly with no conflict — but that guarantee holds
only as long as nobody edits existing lines.

Format: `- YYYY-MM-DD — <area>: <what changed> (PR #N)`

---

- 2026-06-14 — docs: Duster v1 design decision log finalized (#1–20)
- 2026-06-14 — duster: env-based config, deployment topology, monorepo Gradle setup (PR #20, #21, #22)
- 2026-08-28 — duster: auth-flow refactor — client-scoped sessions, Duster-side PKCE, HMAC-SHA256 callback signing, JSON callback contract, `DUSTER_ADMIN_TOKEN` gate on `/internal/*`, `PATCH /internal/apps/config`, upsert-safe `/internal/apps/create`, silent session refresh, `SameSite=Strict` cookie, opt-in k8s ingress (PR #23)
- 2026-08-28 — authos+frontend: consent screen renders real client name / scopes / user email via `GET /oauth/client-info`; `/duster/pull` rejects cross-tenant reads (403); `GET`/`POST /test/callback` reference landing route + webhook contract fix (PR #23)
- 2026-08-28 — docs: adaptability review — design decisions #21–30 (integration tier ladder 0–4; v1 ships tiers 0–2)
- 2026-08-28 — ci: commit the root `gradle-wrapper.jar` + add `wrapper-validation` so the e2e workflow can bootstrap `gradlew`
- 2026-08-28 — authos: PKCE end to end at `/oauth/authorize` + `/oauth/token` (S256 only, verify-if-present, RFC 7636 §4.6 downgrade protection) (PR #24)
- 2026-08-28 — e2e-tests: new Gradle module — docker-compose stack (Postgres + Redis + authos-api + duster) + HTTP-level suite + `e2e.yaml` workflow (PR #25)
- 2026-08-29 — authos: `/oauth/token` reports the access token's real `expires_in`; new `authos.oidc.access-token-ttl-seconds` (default 3600); access-token lifetime 24h → 1h (PR #26)
- 2026-08-29 — authos: OIDC discovery doc at `GET /.well-known/openid-configuration` — `issuer` byte-matches the ID token `iss`, capability lists match the implementation (PR #27)
