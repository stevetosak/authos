import { vi } from 'vitest'

/**
 * A `fetch` implementation that speaks the verified Duster wire contract
 * (`duster/src/main/kotlin/routes/{SessionRoutes,OAuthRoutes,Cors}.kt`):
 *
 * - `GET /me` — `client_id` query param required (400 if missing); 200 flat all-string body +
 *   `X-Duster-Csrf` header; 401 empty body; `meStatus` override for 5xx `text/plain`.
 * - `POST /logout` — tier-1 (`tier1: true`) + live session requires `POST` + a matching
 *   `X-Duster-Csrf` header (405 / 403 otherwise); success clears the session and 302s.
 */
export interface FakeDusterState {
  session: boolean
  csrf: string
  meBody: Record<string, string>
  tier1: boolean
  /** Force `/me` to this status (e.g. 500, 503). `null` = normal behaviour. */
  meStatus: number | null
  /** Make `/me` reject with a network error. */
  meNetworkError: boolean
  calls: Array<{ url: string; method: string; headers: Record<string, string> }>
}

const DEFAULT_BODY: Record<string, string> = {
  sub: 'ppid-jane',
  name: 'Jane Doe',
  email: 'jane@example.com',
  emailVerified: 'true',
  givenName: 'Jane',
  familyName: 'Doe',
  phoneNumberVerified: 'false',
}

function headerBag(init?: HeadersInit): Record<string, string> {
  const out: Record<string, string> = {}
  new Headers(init).forEach((value, key) => {
    out[key.toLowerCase()] = value
  })
  return out
}

function json(body: unknown, status: number, headers: Record<string, string> = {}): Response {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'content-type': 'application/json', ...headers },
  })
}

export function fakeDuster(overrides: Partial<FakeDusterState> = {}): {
  state: FakeDusterState
  fetch: typeof fetch
} {
  const state: FakeDusterState = {
    session: true,
    csrf: 'csrf-token-abc',
    meBody: { ...DEFAULT_BODY },
    tier1: false,
    meStatus: null,
    meNetworkError: false,
    calls: [],
    ...overrides,
  }

  const impl = vi.fn(async (input: string | URL | Request, init: RequestInit = {}) => {
    const url = typeof input === 'string' ? input : input.toString()
    const method = (init.method ?? 'GET').toUpperCase()
    const headers = headerBag(init.headers)
    state.calls.push({ url, method, headers })

    const parsed = new URL(url, 'http://duster.test')
    const path = parsed.pathname

    if (path.endsWith('/me') || path.endsWith('/session')) {
      if (state.meNetworkError) throw new TypeError('Failed to fetch')
      if (!parsed.searchParams.get('client_id')) {
        return json({ error: 'client_id required' }, 400)
      }
      if (state.meStatus !== null && state.meStatus !== 200) {
        if (state.meStatus === 401) return new Response(null, { status: 401 })
        return new Response(`${state.meStatus}: simulated failure`, {
          status: state.meStatus,
          headers: { 'content-type': 'text/plain' },
        })
      }
      if (!state.session) return new Response(null, { status: 401 })
      return json(state.meBody, 200, { 'x-duster-csrf': state.csrf })
    }

    if (path.endsWith('/logout')) {
      if (!parsed.searchParams.get('client_id')) {
        return json({ error: 'client_id required' }, 400)
      }
      if (state.tier1 && state.session) {
        if (method !== 'POST') {
          return json({ error: 'this app requires POST /logout with an X-Duster-Csrf header' }, 405)
        }
        const presented = headers['x-duster-csrf']
        if (!presented || presented !== state.csrf) {
          return json({ error: 'missing or invalid X-Duster-Csrf' }, 403)
        }
      }
      state.session = false
      return new Response(null, { status: 302, headers: { location: 'http://app.test/goodbye' } })
    }

    return new Response('not found', { status: 404 })
  })

  return { state, fetch: impl as unknown as typeof fetch }
}
