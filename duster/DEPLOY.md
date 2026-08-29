# Deploying Duster

Duster is a stateless Ktor service. It keeps everything in **Redis** and talks to one **Authos**
IDP. There is no database. That makes it portable: one image, a handful of env vars, and any of the
targets below.

- [The one decision you must make](#the-one-decision-you-must-make)
- [Configuration](#configuration)
- [Targets](#targets)
  - [`docker run`](#docker-run)
  - [docker-compose](#docker-compose)
  - [Kubernetes](#kubernetes)
  - [Bare JVM](#bare-jvm)
- [Wiring an app to a deployed Duster](#wiring-an-app-to-a-deployed-duster)
- [Health & operations](#health--operations)

---

## The one decision you must make

**Which host serves `…/duster/api/v1/oauth/callback`?** That URL is registered on the Authos app
(the redirect URI containing `/duster/api/v1/oauth/callback`), and Authos redirects the browser back
to it after login. Duster has no other notion of its own address.

- **Tier 0 / 2** — Duster is reverse-proxied under `/duster` on your app's own origin. The callback
  host is your app's host: `https://app.example.com/duster/api/v1/oauth/callback`.
- **Tier 1** — Duster is on its own hostname and your app registers `allowed_origins`. The callback
  host is Duster's: `https://duster.example.com/duster/api/v1/oauth/callback`.

Everything else is just plumbing.

---

## Configuration

All via environment variables.

| Variable | Required | Default | Notes |
|---|---|---|---|
| `AUTHOS_BASE_URL` | **yes** in any non-local deploy | `http://<HOST_IP>:8080` | Base URL of the Authos IDP. **Must byte-match the Authos discovery `issuer`** — Duster verifies the ID token `iss` against it and loads JWKS from it. |
| `DUSTER_ADMIN_TOKEN` | **yes** to provision | _(unset ⇒ every `/internal/*` call 401s)_ | Bearer secret for `/duster/api/v1/internal/*` (app registry + CLI credentials). `openssl rand -hex 32`. Must match `dstr`'s admin token. |
| `REDIS_URL` | one of `REDIS_URL` / `REDIS_HOST` | — | `redis[s]://[:password@]host:port[/db]`. One var covering TLS, auth and db index — use it for any managed Redis (Redis Cloud, Upstash, ElastiCache…). Wins over the discrete vars below. |
| `REDIS_HOST` | one of `REDIS_URL` / `REDIS_HOST` | `localhost` | |
| `REDIS_PORT` | no | `6379` | |
| `REDIS_PASSWORD` | no | — | URL-encoded into the connection URI for you. |
| `REDIS_USESSL` | no | `false` | `true` ⇒ `rediss://`. |
| `REDIS_TIMEOUT` | no | `60` | Connection timeout, seconds. |
| `PORT` | no | `8785` | Listen port. |
| `HOST_IP` | no | `localhost` | Only used to rewrite `localhost` in a webhook callback URI for Docker/VM setups. Irrelevant to tier 0. |

---

## Targets

### `docker run`

```bash
docker run -d --name duster -p 8785:8785 \
  -e AUTHOS_BASE_URL=https://auth.example.com \
  -e REDIS_URL=rediss://:$REDIS_PASSWORD@redis.example.com:6380 \
  -e DUSTER_ADMIN_TOKEN=$(openssl rand -hex 32) \
  stevetosak/authos-duster:latest
```

### docker-compose

Three files ship in this directory:

| File | Redis |
|---|---|
| `docker-compose.yml` | bundled `redis:7-alpine` — local dev (`docker compose up --build`) |
| `docker-compose.yml` + `docker-compose.external-redis.yml` | your Redis (`REDIS_URL` or `REDIS_HOST`) |
| `docker-compose.prod.yml` | your Redis; no bundled service; expects all vars from the environment / a `.env` |

```bash
# production
AUTHOS_BASE_URL=https://auth.example.com \
REDIS_URL=rediss://:secret@redis.example.com:6380 \
DUSTER_ADMIN_TOKEN=$(openssl rand -hex 32) \
docker compose -f docker-compose.prod.yml up -d
```

### Kubernetes

`k8s/` is a Kustomize **base** plus an example **overlay**.

```bash
# copy the example, edit values (namespace, image tag, AUTHOS_BASE_URL, Redis, secrets), then:
kubectl apply -k k8s/overlays/example
```

The base ships:

- `Deployment` — `runAsNonRoot`, read-only rootfs, all capabilities dropped; `livenessProbe` →
  `/health/live` (process only — a Redis blip never restarts the pod), `readinessProbe` → `/health`
  (pings Redis — a Redis outage pulls the pod from the Service).
- `Service` — `ClusterIP` on `8785` (port name `http`).

The overlay supplies the `namespace`, the image (`images:` transform), and the `duster-config`
ConfigMap + `duster-secrets` Secret (via generators). In a real GitOps repo the overlay is a copy of
`overlays/example` with the secret sourced from your secret manager, not committed literals.

Replicas are safe to scale — all state is in Redis, JWT verification is stateless.

**Ingress.** `k8s/optional/ingress.yml` is a **tier-1** reference: Duster on its own host. For
**tier 0**, don't use it — add a path rule to the *consuming app's* Ingress instead:

```yaml
- path: /duster
  pathType: Prefix          # NO rewrite-target — Duster serves the full /duster/api/v1/... path
  backend:
    service: { name: duster-client, port: { name: http } }
```

### Bare JVM

```bash
./gradlew buildFatJar        # -> build/libs/fat.jar
AUTHOS_BASE_URL=https://auth.example.com \
REDIS_HOST=redis.example.com \
DUSTER_ADMIN_TOKEN=... \
java -jar build/libs/fat.jar
```

Any JRE ≥ 17.

---

## Wiring an app to a deployed Duster

Provisioning is the same for every target — it happens over HTTP against Authos + Duster, not on the
box Duster runs on. In Authos: register the OAuth app with a redirect URI containing
`…/duster/api/v1/oauth/callback`, set its `dusterCallbackUri`, and enable a Duster service account.
Then, pointed at the deployed hosts:

```bash
dstr --host https://duster.example.com --authos-host https://auth.example.com \
     --admin-token $DUSTER_ADMIN_TOKEN \
     credentials save -cid <svc-client-id> -cs <svc-client-secret>
dstr ... sync -cid <app-client-id>
dstr ... apps configure -cid <app-client-id> --success-url / --error-url /error
```

`dstr sync` pulls the app's config from Authos and writes it into Duster's Redis. See
`dstr-cli/CLAUDE.md`.

---

## Health & operations

| Endpoint | Use | Behaviour |
|---|---|---|
| `GET /health/live` | k8s liveness, "is it up" | `200 {"status":"ok"}` — never touches Redis |
| `GET /health` | k8s readiness, compose healthcheck, "can it serve" | `200 {"status":"ok","redis":"connected"}`, or `503` when Redis is unreachable |

- The image runs as UID `10001`, read-only rootfs, and carries a `HEALTHCHECK` hitting
  `/health/live`.
- `JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=75` is baked in; override it if you set a different memory
  limit.
- Logs go to stdout (Logback). Redis connection URIs are logged with the password redacted.
