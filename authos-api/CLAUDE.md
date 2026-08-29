# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./mvnw clean package

# Run
./mvnw spring-boot:run

# Run all tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=OAuthFlowsTest

# Run a single test method
./mvnw test -Dtest=UnitTests#methodName
```

## Architecture

**Authos** is a custom OpenID Connect (OIDC) Identity Provider built with Spring Boot 3 (Kotlin + Java mixed). It implements the Authorization Code flow and issues JWTs signed with RS256 via Nimbus JOSE+JWT.

### Key Abstractions

**AppGroup** — Apps are organized into groups. Groups define SSO policy (Full/Partial/Same Domain/Disabled) and MFA policy. Apps in the same group share an SSO session.

**PPID (Pairwise Pseudonymous Identifier)** — Users are identified by a per-group pseudonymous subject (`sub`). The PPID is `hex(SHA-256(groupId + userId + salt))`, stored in the `ppid` table. This means a user has a different `sub` for each AppGroup, preventing cross-app tracking.

**ShortSession** — A short-lived Redis entry created at `/oauth/authorize` that stores the authorization request parameters (client_id, redirect_uri, scope, nonce, etc.) keyed by an `authz_id`. It bridges the authorize → login → consent → approve steps.

**SSOSession** — A longer-lived Redis session created on login. Stores auth time and the active app. Used to determine if re-authentication is needed and to enforce `max_age` and `prompt` semantics.

**Duster** — A companion integration feature. Duster apps can call `/duster/pull` with a `client_credentials` access token to fetch their own OIDC client credentials (clientId, clientSecret, redirectUri). This allows server-side apps to self-configure from Authos.

### Authorization Flow

```
/oauth/authorize
  → if no session / prompt=login  → redirect to {frontendHost}/oauth/login
  → if session exists (no prompt)  → /oauth/approve (auto-approve)
  → if prompt=none                 → AuthorizationHandler.silentApprove(): mint the code
                                     straight from the SSOSession, 302 to redirect_uri?code=…
                                     (no /oauth/approve hop — that path needs the short-lived
                                     AUTH_TOKEN cookie, which can lapse while the SSO session
                                     lives). No usable session → error=login_required.
                                     prompt=none + offline_access → invalid_request (no
                                     consent persistence yet).
  → if prompt=consent              → redirect to {frontendHost}/oauth/user-consent

/oauth-login (POST)        — validates credentials, creates SSOSession, redirects to consent
/oauth/approve (GET)       — mints the code from the server-side ShortSession (not the
                             carried-through query params), cross-checks client_id/redirect_uri,
                             binds the code to the SSOSession
/oauth/token (POST)        — exchanges code for access_token + id_token (+ refresh_token if offline_access)
/oauth/revoke (POST)       — RFC 7009; client-authenticated, always 200; revoking a refresh
                             token cascades to that grant's access tokens
/oauth/userinfo            — resolves claims from access token
```

### OIDC Discovery & token lifetime

- `GET /.well-known/openid-configuration` (`PublicEndpointsController` + `OpenIdProviderMetadata`)
  — `issuer` byte-matches the ID token `iss`; capability lists reflect what is actually
  implemented; advertises `revocation_endpoint`.
- Access-token lifetime is `authos.oidc.access-token-ttl-seconds` (env `ACCESS_TOKEN_TTL_SECONDS`,
  default 3600). The same key drives both `AccessToken.expiresAt` and the `expires_in` reported
  by `/oauth/token`.

### OAuth error contract

`/oauth/token` returns RFC 6749 §5.2 JSON (`{"error": "...", "error_description": "..."}`),
`invalid_client` → 401. `/oauth/authorize` failures redirect back with `?error=…` when a
redirect_uri is known. `api/rest/ExceptionHandler.kt` has a path-aware catch-all that keeps this
consistent — no raw 500 / whitelabel page on the OAuth paths. Typed exceptions
(`TokenEndpointException`, `AuthorizationEndpointException`, both extending `OAuth2Exception`)
instead of raw `Exception`/`TODO()`.

### Token Strategy Pattern

`JwtTokenFactory` creates JWTs using pluggable `JwtTokenStrategy` implementations:
- `LoginTokenStrategy` — short-lived auth token (stored in `AUTH_TOKEN` cookie), subject = PPID sub
- `IdTokenStrategy` — OIDC ID token returned from `/oauth/token`
- `MFATokenStrategy` — token issued during MFA challenge
- `RedirectResponseTokenStrategy` — used to sign redirect URLs to prevent tampering

### Infrastructure

- **PostgreSQL** — persistent store for users, apps, groups, tokens, PPIDs. Schema managed by Flyway (`src/main/resources/db/migration/`).
- **Redis** — stores SSO sessions and short sessions (ShortSession, SSOSession POJOs serialized via Jackson).
- **PKCS12 Keystore** — holds RSA key pair (`authos-jwt-sign`) for JWT signing and AES key (`authos-credentials-encrypt`) for encrypting client secrets at rest.

### Request Authentication

`JwtFilter` runs on all requests except explicitly excluded paths. It reads the JWT from the `AUTH_TOKEN` cookie or `Authorization: Bearer` header, verifies it via `JwtService`, and populates `SecurityContextHolder`. Excluded paths (`JwtFilter.excludedPaths`): `/native-login`, `/oauth-login`, `/register`, `/oauth/authorize`, `/oauth/token`, `/oauth/revoke`, `/oauth/userinfo`, `/.well-known/*`, `/test/*`, `/duster/pull`, `/duster/validate-token`, `/verify-sub`. Note `/oauth/approve` is **not** excluded — it authenticates off the `AUTH_TOKEN` cookie, which is why `prompt=none` bypasses it (see Authorization Flow).

### Configuration

All secrets and environment-specific values are loaded via `.env` (dotenv-java) and referenced in `application.properties` as `${ENV_VAR}`. Required variables:

| Variable | Purpose |
|---|---|
| `DB_USER`, `DB_PASS`, `DB_HOST`, `DB_PORT`, `DB_NAME` | PostgreSQL connection |
| `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASS` | Redis connection |
| `KEYSTORE_PATH`, `KEYSTORE_PASS` | PKCS12 keystore |
| `FRONTEND_HOST`, `API_HOST` | Used in redirect URLs; `API_HOST` is the discovery `issuer` / ID token `iss` |
| `FRONTEND_COOKIE_DOMAIN`, `API_COOKIE_DOMAIN` | Cookie scoping |
| `ACCESS_TOKEN_TTL_SECONDS` | Optional (default 3600) — access-token lifetime + reported `expires_in` |

### Claims

Scope-to-claim mappings are defined in `src/main/resources/scopes_to_claims.yml`. OIDC claim name aliases (e.g. `family_name` → DB column `last_name`) are in `claims_aliases.yml`. `ClaimService` resolves claims from an `AccessToken` using these mappings.

### Package Layout

```
oidc/
  api/
    filter/     JwtFilter
    rest/       Controllers (AuthController, OAuthEndpoints, ApplicationController, DusterEndpoints, ...)
  common/
    dto/        Request/response DTOs
    enums/      GrantType, ScopeType, TokenType, PromptType, ...
    pojo/       Domain POJOs and JWT token strategies
    utils/      Crypto helpers, JwtTokenFactory, demand() assertion util
  config/       Spring configuration (Crypto, Redis, Cache, DotEnv, Beans)
  entity/       JPA entities (App, User, AppGroup, PPID, AccessToken, RefreshToken, ...)
  exceptions/   Exception hierarchy (base, badreq, unauthorized, oauth, internal)
  repository/   Spring Data JPA repositories
  service/      Business logic services
```

### `demand()` Utility

`demand(condition) { exception }` is a custom inline assertion that throws the provided exception if the condition is false. Used throughout service and controller code instead of `require`/`check` for typed exception handling.