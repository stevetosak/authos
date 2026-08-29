import type {
  DusterClient,
  DusterConfig,
  DusterError,
  DusterSnapshot,
  DusterUser,
  UnauthenticatedReason,
} from './types.js'
import { normalizeUser } from './normalize.js'
import { buildLogoutUrl, buildMeUrl, buildStartUrl } from './urls.js'
import { request } from './transport.js'
import { dusterError } from './errors.js'

const CSRF_HEADER = 'X-Duster-Csrf'
const RETURN_TO_KEY = 'duster:return-to'
/** Retry backoff for `/me` on transport / 5xx failures during the initial load only. */
const INIT_RETRY_DELAYS_MS = [400, 1200, 3000]
const LOGOUT_TIMEOUT_MS = 5000

const SERVER_SNAPSHOT: DusterSnapshot = Object.freeze({
  user: null,
  status: 'loading',
  error: null,
})

const hasWindow = (): boolean => typeof window !== 'undefined'
const hasDocument = (): boolean => typeof document !== 'undefined'
const delay = (ms: number): Promise<void> =>
  new Promise((resolve) => setTimeout(resolve, ms))

function currentPath(): string {
  if (!hasWindow()) return '/'
  return window.location.pathname + window.location.search
}

function defaultPostLogout(): string {
  if (!hasWindow()) return '/'
  return window.location.origin + '/'
}

/** Two users are equal iff their raw `/me` maps are — every typed field derives from `raw`. */
function sameUser(a: DusterUser | null, b: DusterUser): boolean {
  if (!a) return false
  const aKeys = Object.keys(a.raw)
  if (aKeys.length !== Object.keys(b.raw).length) return false
  for (const key of aKeys) if (a.raw[key] !== b.raw[key]) return false
  return true
}

function stashReturnTo(value: string): void {
  try {
    if (hasWindow()) window.sessionStorage.setItem(RETURN_TO_KEY, value)
  } catch {
    /* private mode / disabled storage — non-fatal */
  }
}

class DusterClientImpl implements DusterClient {
  #config: DusterConfig
  #snapshot: DusterSnapshot
  #listeners = new Set<() => void>()
  #csrfToken: string | null = null
  #initPromise: Promise<void> | null = null
  #inFlight: Promise<void> | null = null
  #destroyed = false
  #visibilityHandler: (() => void) | null = null
  #onlineHandler: (() => void) | null = null

  constructor(config: DusterConfig) {
    this.#config = config
    this.#snapshot = config.clientId
      ? SERVER_SNAPSHOT
      : Object.freeze({
          user: null,
          status: 'unauthenticated',
          error: dusterError('config', 'duster: clientId is required'),
        })
  }

  getSnapshot(): DusterSnapshot {
    return this.#snapshot
  }

  getServerSnapshot(): DusterSnapshot {
    return SERVER_SNAPSHOT
  }

  subscribe(listener: () => void): () => void {
    this.#listeners.add(listener)
    return () => {
      this.#listeners.delete(listener)
    }
  }

  init(): Promise<void> {
    if (this.#initPromise) return this.#initPromise
    this.#attachRevalidation()
    this.#initPromise = this.#load('init')
    return this.#initPromise
  }

  refresh(): Promise<void> {
    return this.#load('refresh')
  }

  login(opts?: { returnTo?: string }): void {
    if (!this.#config.clientId) {
      console.error('duster: login() called with no clientId')
      return
    }
    if (!hasWindow()) {
      console.warn('duster: login() called with no window; ignoring')
      return
    }
    stashReturnTo(opts?.returnTo ?? currentPath())
    window.location.assign(buildStartUrl(this.#config))
  }

  async logout(opts?: { redirectTo?: string }): Promise<void> {
    const target =
      opts?.redirectTo ?? this.#config.postLogoutRedirect ?? defaultPostLogout()
    if (this.#destroyed || !this.#config.clientId) {
      this.#finishLogout(target)
      return
    }

    // Tier-1 apps require the X-Duster-Csrf token from a prior /me. If we don't hold one, get one.
    if (!this.#csrfToken) {
      try {
        const probe = await request(this.#config, buildMeUrl(this.#config), {
          timeoutMs: LOGOUT_TIMEOUT_MS,
        })
        if (probe.status === 401) {
          this.#finishLogout(target) // already logged out
          return
        }
        const token = probe.headers.get(CSRF_HEADER)
        if (token) this.#csrfToken = token
      } catch {
        /* fall through — attempt a tokenless POST (works for tier 0/2) */
      }
    }

    try {
      const headers: Record<string, string> = {}
      if (this.#csrfToken) headers[CSRF_HEADER] = this.#csrfToken
      await request(this.#config, buildLogoutUrl(this.#config), {
        method: 'POST',
        headers,
        redirect: 'manual',
        timeoutMs: LOGOUT_TIMEOUT_MS,
      })
    } catch {
      /* opaque redirect / network error — local state is cleared regardless */
    }

    this.#finishLogout(target)
  }

  destroy(): void {
    this.#destroyed = true
    this.#listeners.clear()
    this.#detachRevalidation()
  }

  // --- internals -------------------------------------------------------------

  #set(next: Partial<DusterSnapshot>): void {
    const merged: DusterSnapshot = Object.freeze({
      user: next.user !== undefined ? next.user : this.#snapshot.user,
      status: next.status !== undefined ? next.status : this.#snapshot.status,
      error: next.error !== undefined ? next.error : this.#snapshot.error,
    })
    if (
      merged.user === this.#snapshot.user &&
      merged.status === this.#snapshot.status &&
      merged.error === this.#snapshot.error
    ) {
      return
    }
    this.#snapshot = merged
    for (const listener of this.#listeners) listener()
  }

  #finishLogout(target: string): void {
    this.#csrfToken = null
    this.#set({ user: null, status: 'unauthenticated', error: null })
    if (hasWindow()) window.location.assign(target)
  }

  #dispatchUnauthenticated(reason: UnauthenticatedReason): void {
    const behaviour = this.#config.onUnauthenticated ?? 'redirect'
    if (behaviour === 'ignore') return
    const returnTo = currentPath()
    if (behaviour === 'redirect') {
      this.login({ returnTo })
      return
    }
    behaviour({ reason, returnTo, login: () => this.login({ returnTo }) })
  }

  #load(source: 'init' | 'refresh' | 'revalidate'): Promise<void> {
    if (this.#inFlight) return this.#inFlight
    const run = this.#doLoad(source).finally(() => {
      this.#inFlight = null
    })
    this.#inFlight = run
    return run
  }

  async #doLoad(source: 'init' | 'refresh' | 'revalidate'): Promise<void> {
    if (this.#destroyed || !this.#config.clientId) return

    const wasAuthenticated = this.#snapshot.status === 'authenticated'
    const maxAttempts = source === 'init' ? INIT_RETRY_DELAYS_MS.length + 1 : 1
    let lastError: DusterError | null = null

    for (let attempt = 0; attempt < maxAttempts; attempt++) {
      if (this.#destroyed) return

      let res
      try {
        res = await request(this.#config, buildMeUrl(this.#config))
      } catch (err) {
        lastError = dusterError(
          'network',
          err instanceof Error ? err.message : 'network error',
        )
        if (attempt < maxAttempts - 1) {
          await delay(INIT_RETRY_DELAYS_MS[attempt] ?? 0)
          continue
        }
        break
      }

      if (res.status === 200) {
        const token = res.headers.get(CSRF_HEADER)
        if (token) this.#csrfToken = token
        let body: Record<string, unknown>
        try {
          body = JSON.parse(res.text) as Record<string, unknown>
        } catch {
          lastError = dusterError('server', 'duster: /me returned invalid JSON', 200)
          break
        }
        const next = normalizeUser(body)
        const user = sameUser(this.#snapshot.user, next) ? this.#snapshot.user : next
        this.#set({ user, status: 'authenticated', error: null })
        return
      }

      if (res.status === 401) {
        this.#csrfToken = null
        const reason: UnauthenticatedReason = wasAuthenticated
          ? 'revalidation-failed'
          : 'no-session'
        this.#set({ user: null, status: 'unauthenticated', error: null })
        this.#dispatchUnauthenticated(reason)
        return
      }

      if (res.status === 400) {
        // We always send client_id, so a 400 means a genuine config problem.
        lastError = dusterError(
          'config',
          `duster: /me rejected the request (400)${res.text ? ': ' + res.text.trim() : ''}`,
          400,
        )
        break
      }

      // 5xx / 3xx / unexpected — a transport or server problem, NOT an auth failure.
      lastError = dusterError('server', `duster: /me failed (${res.status})`, res.status)
      if (attempt < maxAttempts - 1) {
        await delay(INIT_RETRY_DELAYS_MS[attempt] ?? 0)
        continue
      }
      break
    }

    // Exhausted: surface the error, keep status/user as they were (loading stays loading).
    if (lastError) this.#set({ error: lastError })
  }

  #attachRevalidation(): void {
    if (this.#config.revalidateOnFocus && hasDocument()) {
      this.#visibilityHandler = () => {
        if (document.visibilityState === 'visible') void this.#load('revalidate')
      }
      document.addEventListener('visibilitychange', this.#visibilityHandler)
    }
    if (this.#config.revalidateOnReconnect && hasWindow()) {
      this.#onlineHandler = () => void this.#load('revalidate')
      window.addEventListener('online', this.#onlineHandler)
    }
  }

  #detachRevalidation(): void {
    if (this.#visibilityHandler && hasDocument()) {
      document.removeEventListener('visibilitychange', this.#visibilityHandler)
    }
    if (this.#onlineHandler && hasWindow()) {
      window.removeEventListener('online', this.#onlineHandler)
    }
    this.#visibilityHandler = null
    this.#onlineHandler = null
  }
}

/** Create an unmanaged client. Most apps use {@link getOrCreateDusterClient} instead. */
export function createDusterClient(config: DusterConfig): DusterClient {
  return new DusterClientImpl(config)
}
