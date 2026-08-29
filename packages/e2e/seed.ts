/**
 * Brings a fresh Authos + Duster stack up to the point where the example SPA can run a real
 * tier-0 login: a user, an OAuth app wired for Duster, a Duster service account, and that app
 * synced into Duster's Redis. A TypeScript port of
 * `e2e-tests/src/test/kotlin/com/tosak/authos/e2e/support/{SeedClient,DusterSync}.kt`.
 *
 * Writes `.fixture.json` (git-ignored) for the specs to read.
 */
import { writeFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

const AUTHOS = process.env.E2E_AUTHOS ?? 'http://localhost:18080'
const DUSTER = process.env.E2E_DUSTER ?? 'http://localhost:18785'
const ADMIN_TOKEN = process.env.E2E_DUSTER_ADMIN_TOKEN ?? 'test-admin-token'

export interface Fixture {
  authos: string
  duster: string
  clientId: string
  user: { email: string; password: string }
}

/** A trivial cookie jar — `name=value` only, single host. Mirrors `support/Http.kt`, which
 *  hand-rolls one because Secure cookies (every Authos cookie is Secure) get dropped by the
 *  stock clients over plain http. */
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
  const res = await fetch(url, { method, headers, body, redirect: 'manual' })
  if (opts.jar) opts.jar.absorb(res)
  return res
}

async function expectStatus(res: Response, want: number, label: string): Promise<Response> {
  if (res.status !== want) {
    throw new Error(`${label} -> ${res.status} ${await res.text()}`)
  }
  return res
}

async function waitFor(url: string, timeoutMs: number): Promise<void> {
  const deadline = Date.now() + timeoutMs
  let last = 'no attempt'
  while (Date.now() < deadline) {
    try {
      const r = await fetch(url, { signal: AbortSignal.timeout(3000) })
      if (r.status === 200) return
      last = `HTTP ${r.status}`
    } catch (e) {
      last = e instanceof Error ? e.message : String(e)
    }
    await new Promise((r) => setTimeout(r, 1000))
  }
  throw new Error(`timed out waiting for ${url} (${last})`)
}

export async function seed(): Promise<Fixture> {
  await waitFor(`${AUTHOS}/.well-known/jwks.json`, 180_000)
  await waitFor(`${DUSTER}/health`, 60_000)

  const user = { email: `sdk-e2e+${Date.now()}@example.com`, password: 'TestPass123!' }

  await expectStatus(
    await req('POST', `${AUTHOS}/register`, {
      json: { email: user.email, password: user.password, name: 'SDK', surname: 'E2E' },
    }),
    201,
    'register',
  )

  const jar = new Jar()
  await expectStatus(
    await req('POST', `${AUTHOS}/native-login`, { jar, form: { email: user.email, password: user.password } }),
    200,
    'native-login',
  )

  const clientId = await registerDusterApp(jar)
  const svc = await createServiceAccount(jar)
  await saveCredentials(svc)
  await dusterSync(clientId, svc)

  const fixture: Fixture = { authos: AUTHOS, duster: DUSTER, clientId, user }
  writeFileSync(fileURLToPath(new URL('./.fixture.json', import.meta.url)), JSON.stringify(fixture, null, 2))
  console.log(`[seed] ready — client_id=${clientId} user=${user.email}`)
  return fixture
}

async function registerDusterApp(jar: Jar): Promise<string> {
  const redirect = `${DUSTER}/duster/api/v1/oauth/callback`
  const registered = (await (
    await expectStatus(
      await req('POST', `${AUTHOS}/app/register`, {
        jar,
        json: {
          appName: 'sdk-e2e-duster',
          shortDescription: 'browser e2e app',
          tokenEndpointAuthMethod: 'client_secret_post',
          grantTypes: ['authorization_code'],
          responseTypes: ['code'],
          redirectUris: [redirect],
          scope: ['openid', 'profile', 'email', 'offline_access'],
          group: null,
        },
      }),
      201,
      'app/register',
    )
  ).json()) as Record<string, unknown>

  // /app/register can't set duster_callback_uri and /duster/pull requires it non-null. There is no
  // GET-one endpoint — post the register DTO straight back to /app/update with the two fields set.
  const updated = { ...registered, dusterCallbackUri: `${DUSTER}/e2e-webhook`, redirectUris: [redirect] }
  await expectStatus(
    await req('POST', `${AUTHOS}/app/update`, { jar, json: updated }),
    201,
    'app/update',
  )

  return registered['clientId'] as string
}

async function createServiceAccount(jar: Jar): Promise<{ clientId: string; clientSecret: string }> {
  const dto = (await (
    await expectStatus(await req('POST', `${AUTHOS}/duster/create`, { jar, json: {} }), 201, 'duster/create')
  ).json()) as { clientId: string; clientSecret: string }
  return dto
}

async function saveCredentials(svc: { clientId: string; clientSecret: string }): Promise<void> {
  const url = `${DUSTER}/duster/api/v1/internal/credentials/save?client_id=${encodeURIComponent(
    svc.clientId,
  )}&client_secret=${encodeURIComponent(svc.clientSecret)}`
  await expectStatus(await req('POST', url, { adminAuth: true }), 200, 'internal/credentials/save')
}

/** The `dstr sync` sequence for one app, over HTTP — port of `support/DusterSync.kt`. */
async function dusterSync(appClientId: string, svc: { clientId: string; clientSecret: string }): Promise<void> {
  const token = (await (
    await expectStatus(
      await req('POST', `${AUTHOS}/oauth/token`, {
        form: {
          grant_type: 'client_credentials',
          client_id: svc.clientId,
          client_secret: svc.clientSecret,
        },
      }),
      200,
      'client_credentials token',
    )
  ).json()) as { access_token: string }

  const pull = await expectStatus(
    await req('POST', `${AUTHOS}/duster/pull?client_id=${encodeURIComponent(appClientId)}`, {
      headers: { authorization: `Bearer ${token.access_token}` },
    }),
    200,
    'duster/pull',
  )
  const appSync = await pull.json()

  await expectStatus(
    await req('POST', `${DUSTER}/duster/api/v1/internal/apps/create`, { json: appSync, adminAuth: true }),
    200,
    'internal/apps/create',
  )
}

// `npm run seed` / `tsx seed.ts`
if (import.meta.url === `file://${process.argv[1]}`) {
  seed().catch((e) => {
    console.error(e)
    process.exit(1)
  })
}
