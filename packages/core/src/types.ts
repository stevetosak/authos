/**
 * Session state as seen by the app. Starts at `loading`; the first `/me` resolves it to
 * `authenticated` or `unauthenticated`. A server/network error does NOT move it off its current
 * value — check `DusterSnapshot.error` for that.
 */
export type DusterStatus = 'loading' | 'authenticated' | 'unauthenticated'

/**
 * Normalized userinfo from `GET /duster/api/v1/me`. Duster returns every value as a string; this
 * type coerces the two booleans and keeps the untouched map in `raw` so claims not yet promoted
 * to the typed surface (or added by a future Duster) are still readable.
 */
export interface DusterUser {
  /** Pairwise (PPID) subject identifier. Always present. */
  sub: string
  emailVerified: boolean
  phoneNumberVerified: boolean
  name?: string
  email?: string
  givenName?: string
  familyName?: string
  middleName?: string
  nickname?: string
  preferredUsername?: string
  profile?: string
  picture?: string
  website?: string
  gender?: string
  birthdate?: string
  zoneinfo?: string
  locale?: string
  /** OIDC `updated_at` — epoch seconds as a string. Not coerced to a number/Date. */
  updatedAt?: string
  address?: string
  phoneNumber?: string
  /** The `/me` body verbatim, every value a string. Includes claims not in the typed fields above. */
  raw: Record<string, string>
}

export type DusterErrorKind = 'server' | 'network' | 'config' | 'oauth'

export interface DusterError {
  kind: DusterErrorKind
  /** HTTP status, when the failure came with one. */
  status?: number
  message: string
}

export interface DusterSnapshot {
  readonly user: DusterUser | null
  readonly status: DusterStatus
  readonly error: DusterError | null
}

export type UnauthenticatedReason = 'no-session' | 'revalidation-failed'

export interface UnauthenticatedContext {
  reason: UnauthenticatedReason
  /** Path the user was on when the session was found missing — `pathname + search`. */
  returnTo: string
  /** Start the login redirect (stashes `returnTo` for the app to consume after callback). */
  login: () => void
}

/**
 * What to do when `/me` says the user is not authenticated.
 * - `'redirect'` (default) — immediately start the login flow.
 * - `'ignore'` — do nothing; the app reads `status` and decides.
 * - a function — called with context; you drive navigation.
 */
export type OnUnauthenticated =
  | 'redirect'
  | 'ignore'
  | ((ctx: UnauthenticatedContext) => void)

export interface DusterConfig {
  /** OAuth client id of the Duster-registered app. Required. */
  clientId: string
  /**
   * Origin of the Duster deployment. Default `''` → same-origin (tier 0: `/duster` reverse-proxied
   * onto the SPA's own origin). Set to e.g. `'https://auth.example.com'` for a cross-origin (tier 1)
   * app that registered `allowed_origins`.
   */
  baseUrl?: string
  /** Path Duster is mounted at. Default `'/duster/api/v1'`. */
  basePath?: string
  onUnauthenticated?: OnUnauthenticated
  /** Where to send the browser after `logout()` clears local state. Default `location.origin + '/'`. */
  postLogoutRedirect?: string
  /** Re-check `/me` once when the tab becomes visible again. Not polling. Default `false`. */
  revalidateOnFocus?: boolean
  /** Re-check `/me` once on the `online` event. Default `false`. */
  revalidateOnReconnect?: boolean
  /** Inject a `fetch` implementation (tests, non-browser hosts). Default `globalThis.fetch`. */
  fetch?: typeof fetch
  /**
   * Reserved for tier 4 (opaque bearer session token instead of a cookie). Not used in v1 — present
   * so the config shape does not need a breaking change when tier 4 lands (design decision #30).
   */
  sessionToken?: string | (() => string | null | undefined)
}

export interface DusterClient {
  /** Current state. Frozen; referentially stable until a real change. */
  getSnapshot(): DusterSnapshot
  /** Constant `{ user: null, status: 'loading', error: null }` — for SSR / `useSyncExternalStore`. */
  getServerSnapshot(): DusterSnapshot
  /** Register a change listener. Returns an unsubscribe function. */
  subscribe(listener: () => void): () => void
  /** First `/me` fetch. Idempotent — concurrent callers share one request. Attaches revalidation. */
  init(): Promise<void>
  /** Force a `/me` re-check now. */
  refresh(): Promise<void>
  /** Synchronous — stashes `returnTo` then `window.location.assign` to `/oauth/start`. */
  login(opts?: { returnTo?: string }): void
  /** POST `/logout` (best-effort), clear local state, navigate to `postLogoutRedirect`. */
  logout(opts?: { redirectTo?: string }): Promise<void>
  /** Drop listeners and revalidation handlers. */
  destroy(): void
}
