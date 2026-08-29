/**
 * One-time provisioning for the public demo, run against the live stack.
 *
 * Creates (or reuses) an OAuth app on Authos wired for Duster, a Duster service account, and the
 * app synced into Duster's Redis — the state the SPA needs for a real tier-0 login. A TypeScript
 * port of `packages/e2e/seed.ts`, itself a port of the Kotlin e2e `SeedClient` / `DusterSync`.
 *
 * Re-entrant: if a Duster app named `authos-demo` already exists it reuses that `client_id` and
 * only re-applies the config. Registering a fresh Authos app mints a *new* `client_id`, so run
 * this once and feed the printed value to the deployment as `DEMO_DUSTER_CLIENT_ID`.
 *
 *   DEMO_OWNER_EMAIL=...  DEMO_OWNER_PASSWORD=...  DEMO_DUSTER_ADMIN_TOKEN=... \
 *   [DEMO_AUTHOS=https://authos-api.tosak.net] \
 *   [DEMO_DUSTER=http://localhost:8785]        # a port-forward of the duster Service \
 *   [DEMO_PUBLIC_ORIGIN=https://authos-demo.tosak.net] \
 *   npm run bootstrap
 */

const AUTHOS = trimSlash(process.env.DEMO_AUTHOS ?? 'https://authos-api.tosak.net')
const DUSTER = trimSlash(process.env.DEMO_DUSTER ?? 'http://localhost:8785')
const PUBLIC_ORIGIN = trimSlash(process.env.DEMO_PUBLIC_ORIGIN ?? 'https://authos-demo.tosak.net')
const OWNER_EMAIL = required('DEMO_OWNER_EMAIL')
const OWNER_PASSWORD = required('DEMO_OWNER_PASSWORD')
const ADMIN_TOKEN = required('DEMO_DUSTER_ADMIN_TOKEN')

const APP_NAME = 'authos-demo'
const REDIRECT_URI = `${PUBLIC_ORIGIN}/duster/api/v1/oauth/callback`

function trimSlash(s: string): string {
  return s.replace(/\/+$/, '')
}

function required(name: string): string {
  const v = process.env[name]
  if (!v) {
    console.error(`missing required env ${name}`)
    process.exit(2)
  }
  return v
}

/** name=value cookie jar, single host — Authos cookies are all `Secure` and the stock clients
 *  drop them over plain http, so we replay them by hand. Mirrors `packages/e2e/seed.ts`. */
class Jar {
  private readonly cookies = new Map<string, string>()
  header(): string {
    return [...this.cookies].map(([k, v]) => `${k}=${v}`).join('; ')
  }
  absorb(res: Response): void {
    for (const raw of res.headers.getSetCookie()) {
      const pair = raw.split(';', 1)[0]!.trim()
      const eq = pair.indexOf('=')
      if (eq < 0) continue
      const name = pair.slice(0, eq)
      const value = pair.slice(eq + 1)
      if (!value || value === '""') this.cookies.delete(name)
      else this.cookies.set(name, value)
    }
  }
}

interface ReqOpts {
  jar?: Jar
  form?: Record<string, string>
  json?: unknown
  headers?: Record<string, string>
  adminAuth?: boolean
  bearer?: string
}

async function req(method: string, url: string, opts: ReqOpts = {}): Promise<Response> {
  const headers: Record<string, string> = { ...opts.headers }
  let body: string | undefined
  if (opts.form) {
    headers['content-type'] = 'application/x-www-form-urlencoded'
    body = new URLSearchParams(opts.form).toString()
  }
  if (opts.json !== undefined) {
    headers['content-type'] = 'application/json'
    body = JSON.stringify(opts.json)
  }
  if (opts.jar) {
    const c = opts.jar.header()
    if (c) headers['cookie'] = c
  }
  if (opts.adminAuth) headers['authorization'] = `Bearer ${ADMIN_TOKEN}`
  if (opts.bearer) headers['authorization'] = `Bearer ${opts.bearer}`
  const res = await fetch(url, { method, headers, body, redirect: 'manual' })
  if (opts.jar) opts.jar.absorb(res)
  return res
}

async function expectStatus(res: Response, want: number, label: string): Promise<Response> {
  if (res.status !== want) throw new Error(`${label} -> ${res.status} ${await res.text()}`)
  return res
}

async function waitFor(url: string, timeoutMs: number): Promise<void> {
  const deadline = Date.now() + timeoutMs
  let last = 'no attempt'
  while (Date.now() < deadline) {
    try {
      const r = await fetch(url, { signal: AbortSignal.timeout(3000) })
      if (r.ok) return
      last = `HTTP ${r.status}`
    } catch (e) {
      last = e instanceof Error ? e.message : String(e)
    }
    await new Promise((r) => setTimeout(r, 1000))
  }
  throw new Error(`timed out waiting for ${url} (${last})`)
}

async function findExistingClientId(): Promise<string | null> {
  const res = await req(
    'GET',
    `${DUSTER}/duster/api/v1/internal/apps?client_name=${encodeURIComponent(APP_NAME)}`,
    { adminAuth: true },
  )
  if (res.status !== 200) return null
  const app = (await res.json()) as { clientId?: string }
  return app.clientId ?? null
}

async function ownerSession(): Promise<Jar> {
  const jar = new Jar()
  const login = await req('POST', `${AUTHOS}/native-login`, {
    jar,
    form: { email: OWNER_EMAIL, password: OWNER_PASSWORD },
  })
  if (login.status === 200) return jar

  // No account yet — register the demo owner, then log in.
  await expectStatus(
    await req('POST', `${AUTHOS}/register`, {
      json: { email: OWNER_EMAIL, password: OWNER_PASSWORD, name: 'Demo', surname: 'Owner' },
    }),
    201,
    'register demo-owner',
  )
  const jar2 = new Jar()
  await expectStatus(
    await req('POST', `${AUTHOS}/native-login`, {
      jar: jar2,
      form: { email: OWNER_EMAIL, password: OWNER_PASSWORD },
    }),
    200,
    'native-login demo-owner',
  )
  return jar2
}

async function registerAuthosApp(jar: Jar): Promise<string> {
  const registerDto = {
    appName: APP_NAME,
    shortDescription: 'The Handshake Trace — public guided demo',
    tokenEndpointAuthMethod: 'client_secret_post',
    grantTypes: ['authorization_code'],
    responseTypes: ['code'],
    redirectUris: [REDIRECT_URI],
    scope: ['openid', 'profile', 'email', 'offline_access'],
    group: null,
  }
  const registered = (await (
    await expectStatus(
      await req('POST', `${AUTHOS}/app/register`, { jar, json: registerDto }),
      201,
      'app/register',
    )
  ).json()) as Record<string, unknown>

  // /app/register can't set duster_callback_uri and /duster/pull rejects a null one. Repost the
  // register DTO to /app/update with the two extra fields (tier 0 never fires the webhook).
  await expectStatus(
    await req('POST', `${AUTHOS}/app/update`, {
      jar,
      json: { ...registered, dusterCallbackUri: `${PUBLIC_ORIGIN}/duster/api/v1/webhook`, redirectUris: [REDIRECT_URI] },
    }),
    201,
    'app/update',
  )
  return registered['clientId'] as string
}

async function createServiceAccount(jar: Jar): Promise<{ clientId: string; clientSecret: string }> {
  return (await (
    await expectStatus(await req('POST', `${AUTHOS}/duster/create`, { jar, json: {} }), 201, 'duster/create')
  ).json()) as { clientId: string; clientSecret: string }
}

async function saveCredentials(svc: { clientId: string; clientSecret: string }): Promise<void> {
  const url = `${DUSTER}/duster/api/v1/internal/credentials/save?client_id=${encodeURIComponent(
    svc.clientId,
  )}&client_secret=${encodeURIComponent(svc.clientSecret)}`
  await expectStatus(await req('POST', url, { adminAuth: true }), 200, 'internal/credentials/save')
}

async function dusterSync(appClientId: string, svc: { clientId: string; clientSecret: string }): Promise<void> {
  const token = (await (
    await expectStatus(
      await req('POST', `${AUTHOS}/oauth/token`, {
        form: { grant_type: 'client_credentials', client_id: svc.clientId, client_secret: svc.clientSecret },
      }),
      200,
      'client_credentials token',
    )
  ).json()) as { access_token: string }

  const appSync = await (
    await expectStatus(
      await req('POST', `${AUTHOS}/duster/pull?client_id=${encodeURIComponent(appClientId)}`, {
        bearer: token.access_token,
      }),
      200,
      'duster/pull',
    )
  ).json()

  await expectStatus(
    await req('POST', `${DUSTER}/duster/api/v1/internal/apps/create`, { json: appSync, adminAuth: true }),
    200,
    'internal/apps/create',
  )
}

async function applyConfig(clientId: string): Promise<void> {
  await expectStatus(
    await req('PATCH', `${DUSTER}/duster/api/v1/internal/apps/config?client_id=${encodeURIComponent(clientId)}`, {
      adminAuth: true,
      // success_url / logout_redirect_url default to "/" (the SPA root, same origin via the
      // `/duster` proxy). Only the failure route needs pointing at the SPA's /error page.
      json: { error_url: '/error' },
    }),
    200,
    'internal/apps/config',
  )
}

async function main(): Promise<void> {
  console.log(`[bootstrap] authos=${AUTHOS} duster=${DUSTER} origin=${PUBLIC_ORIGIN}`)
  await waitFor(`${AUTHOS}/.well-known/jwks.json`, 60_000)
  await waitFor(`${DUSTER}/health/live`, 30_000)

  const existing = await findExistingClientId()
  if (existing) {
    console.log(`[bootstrap] reusing existing Duster app "${APP_NAME}" — client_id=${existing}`)
    await applyConfig(existing)
    report(existing)
    return
  }

  const jar = await ownerSession()
  const clientId = await registerAuthosApp(jar)
  const svc = await createServiceAccount(jar)
  await saveCredentials(svc)
  await dusterSync(clientId, svc)
  await applyConfig(clientId)
  report(clientId)
}

function report(clientId: string): void {
  console.log('')
  console.log('  ┌─────────────────────────────────────────────────────────────')
  console.log('  │  DEMO_DUSTER_CLIENT_ID')
  console.log(`  │  ${clientId}`)
  console.log('  └─────────────────────────────────────────────────────────────')
  console.log('')
  console.log('  Set this as the repo variable DEMO_DUSTER_CLIENT_ID (it flows into the')
  console.log('  deploy overlay and entrypoint.sh renders it into config/config.js).')
}

main().catch((e) => {
  console.error(e instanceof Error ? e.message : e)
  process.exit(1)
})
