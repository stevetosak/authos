# e2e-tests

End-to-end + API automation tests for the Authos stack. Proves the real OAuth/OIDC flow —
including **PKCE (RFC 7636)** — works across `authos-api` + `duster` against a real Postgres +
Redis.

## Run it

```bash
./gradlew :e2e-tests:e2eTest
```

That single task:

1. builds `authos-api/target/Authos-1.0.0-alpha.jar` (`mvnw -DskipTests package`) and
   `duster/build/libs/fat.jar` (`:duster:buildFatJar`),
2. generates a throwaway PKCS12 keystore (`build/keystore/keystore.p12`),
3. brings up `docker-compose.e2e.yml` (Postgres + Redis + authos-api + duster) on fixed host
   ports **18080** / **18785** / **15432** / **16379** — chosen to not collide with the IntelliJ
   run configs or a local Postgres/Redis, so it coexists with your dev setup,
4. seeds a tenant (user, OAuth app wired for Duster, a second "direct" app, a Duster service
   account, and syncs the app into Duster),
5. runs the suite, then tears the stack down (`docker compose down -v`).

First run ~1–2 min (image pulls + `mvn package`); after that ~25 s.

Requires: Docker + `docker compose` v2, a JDK 17 for `mvnw`.

### Options

| flag | effect |
|------|--------|
| `-De2e.attach="authos=http://localhost:8080;duster=http://localhost:8785"` | skip compose, run against already-running services (seeding still runs) |
| `-De2e.skipTeardown=true` | leave the stack up after the suite (for debugging / CI log capture) |

## What's covered

| test | asserts |
|------|---------|
| `PkceThroughDusterTest` | a full PKCE-S256 login through Duster: `/start` → login → approve → Duster `/callback` (real `code_verifier` exchange) → `/session` returns userinfo |
| `PkceNegativeTest` | wrong verifier, missing verifier, `plain` method, malformed challenge, and RFC 7636 §4.6 downgrade (verifier with no stored challenge) are all rejected |
| `PkceRegressionTest` | a non-PKCE `authorization_code` flow still issues tokens (verify-if-present) |
| `DusterSessionLifecycleTest` | `/session` works + re-check (silent refresh) + `/logout` invalidates |
| `DusterInternalApiAuthTest` | `/duster/api/v1/internal/*` needs the admin bearer |
| `DusterPullTenantIsolationTest` | a different tenant's token cannot `/duster/pull` someone else's app (`403`) |
| `DusterResyncUpsertTest` | `session_ttl` / `webhook_secret` survive a re-sync |

## How the harness talks to the stack

The frontend login/consent pages are just shells that read query params and call back into the
API — the harness (`support/OAuthFlow.kt`) does that directly with a cookie jar
(`support/Http.kt`), no browser. Service-to-service traffic inside compose uses the
`authos-api` / `duster` hostnames; redirects that leak those hostnames are rewritten to the
host-reachable ports by `Endpoints.rebase()`.

## Known noise

`./gradlew` prints a "Kotlin Gradle plugin was loaded multiple times" warning (`:duster` and
`:e2e-tests` both apply the catalog `kotlin.jvm` alias). It is non-fatal — the proper fix is a
root `build.gradle.kts` with `apply false`, but `:dstr-cli` pins a different Kotlin version and
that change is out of scope here.
