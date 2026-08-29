/**
 * The tier-0 OIDC handshake as a fixed structure: three actors, five steps, an ordered list of
 * messages. Each message is either `observed` — the browser genuinely made this call and we have a
 * live wire event for it — or `narrated`: it happened as a top-level navigation or server-to-server,
 * off the SPA's wire, and we describe it rather than fake a request line for it.
 *
 * The visual trace is this structure. Progress fills the observed messages with real data; the
 * narrated ones stay as the protocol drawing, tagged as narration with a link to the automated proof.
 */

export type Actor = 'browser' | 'duster' | 'authos'
export const ACTORS: { id: Actor; label: string; sub: string }[] = [
  { id: 'browser', label: 'BROWSER', sub: 'the visitor’s tab' },
  { id: 'duster', label: 'DUSTER', sub: '@authoss/duster — the BFF' },
  { id: 'authos', label: 'AUTHOS', sub: 'the OIDC identity provider' },
]

export type StepId = 'redirect' | 'consent' | 'callback' | 'refresh' | 'logout'

export interface Step {
  id: StepId
  ordinal: number
  title: string
  /** One line, present tense, said as it happens. */
  caption: string
  /** The honest account of what the browser can and cannot see at this step. */
  narration: string
  proof?: 'spec' | 'ci'
}

export const STEPS: Step[] = [
  {
    id: 'redirect',
    ordinal: 1,
    title: 'Redirect',
    caption: 'You press RUN. The SDK hands the browser to Duster, which hands it to Authos.',
    narration:
      '`login()` is one synchronous line — `window.location.assign` to `/duster/api/v1/oauth/start`. ' +
      'Duster mints a PKCE `code_verifier`, stores it in Redis for 5 minutes, and 302s you to the Authos ' +
      'authorize endpoint. No token, no secret, no app code beyond that one call.',
  },
  {
    id: 'consent',
    ordinal: 2,
    title: 'Consent',
    caption: 'You sign in and approve the scopes — on Authos, never on this page.',
    narration:
      'This happens entirely on `authos-api.tosak.net`. The demo can’t see your password or the ' +
      'consent screen, and neither can Duster. Authos issues a short-lived authorization `code` bound ' +
      'to this exact `client_id` and `redirect_uri`.',
  },
  {
    id: 'callback',
    ordinal: 3,
    title: 'Callback → session',
    caption: 'Duster trades the code for tokens, verifies them, and starts a session.',
    narration:
      'Authos 302s back to `/duster/api/v1/oauth/callback?code=…`. Duster exchanges the code at ' +
      '`/oauth/token` (server-to-server), verifies the ID token against the Authos JWKS with a ' +
      'byte-exact `issuer` check, writes the tokens to Redis under `duster:token:<client>:<sub>:*`, ' +
      'and sets a `HttpOnly; Secure; SameSite=Lax` cookie. Then it 302s you home. The first thing ' +
      'the SDK does on return is the `GET /me` below — that part you can watch.',
  },
  {
    id: 'refresh',
    ordinal: 4,
    title: 'Silent refresh',
    caption: 'Another /me. Still 200. Duster swapped the upstream token behind it if it was near expiry.',
    narration:
      'There is no timer in the browser. Every `/me` slides the session TTL, and if the upstream ' +
      'access token is close to its `expires_in`, Duster refreshes it against Authos before answering ' +
      '— you just get a 200. The access token’s lifetime is `authos.oidc.access-token-ttl-seconds` ' +
      '(1 hour by default); the browser never sees it.',
  },
  {
    id: 'logout',
    ordinal: 5,
    title: 'Logout → revoke',
    caption: 'POST /logout. Duster revokes the grant at Authos and purges the token keys.',
    narration:
      '`logout()` POSTs `/duster/api/v1/logout`. Duster calls `POST /oauth/revoke` (RFC 7009) at ' +
      'Authos best-effort, deletes every `duster:token:<client>:<sub>:*` key, drops the session, and ' +
      'clears the cookie. That the Redis keys are actually gone is proven on every CI run.',
    proof: 'spec',
  },
]

export interface Message {
  id: string
  step: StepId
  from: Actor
  to: Actor
  /** The wire label — method + path, or the redirect. */
  label: string
  /** Secondary line: what this message carries. */
  detail: string
  mode: 'observed' | 'narrated'
  /** For observed messages: which live wire event fills this in. */
  wireKey?: 'start-nav' | 'init-me' | 'refresh-me' | 'logout'
  /** `alt` / `opt` frame this message belongs to, drawn as a labelled bracket. */
  frame?: string
}

export const MESSAGES: Message[] = [
  {
    id: 'm-start',
    step: 'redirect',
    from: 'browser',
    to: 'duster',
    label: 'GET /oauth/start?client_id',
    detail: 'top-level navigation — login() ran',
    mode: 'observed',
    wireKey: 'start-nav',
  },
  {
    id: 'm-authorize',
    step: 'redirect',
    from: 'duster',
    to: 'authos',
    label: '302 → /oauth/authorize',
    detail: 'code_challenge (S256), scope, state',
    mode: 'narrated',
  },
  {
    id: 'm-login',
    step: 'consent',
    from: 'browser',
    to: 'authos',
    label: 'login + consent',
    detail: 'on authos-api.tosak.net',
    mode: 'narrated',
  },
  {
    id: 'm-code',
    step: 'consent',
    from: 'authos',
    to: 'browser',
    label: '302 → /oauth/callback?code',
    detail: 'short-lived authorization code',
    mode: 'narrated',
  },
  {
    id: 'm-token',
    step: 'callback',
    from: 'duster',
    to: 'authos',
    label: 'POST /oauth/token',
    detail: 'code + code_verifier → id, access, refresh',
    mode: 'narrated',
    frame: 'server-to-server',
  },
  {
    id: 'm-jwks',
    step: 'callback',
    from: 'duster',
    to: 'authos',
    label: 'GET /.well-known/jwks.json',
    detail: 'verify id_token signature + issuer',
    mode: 'narrated',
    frame: 'server-to-server',
  },
  {
    id: 'm-cookie',
    step: 'callback',
    from: 'duster',
    to: 'browser',
    label: '302 → /  ·  Set-Cookie',
    detail: 'duster_session — HttpOnly, Secure, SameSite=Lax',
    mode: 'narrated',
  },
  {
    id: 'm-me-init',
    step: 'callback',
    from: 'browser',
    to: 'duster',
    label: 'GET /me',
    detail: 'the SDK’s first call on return — userinfo + X-Duster-Csrf',
    mode: 'observed',
    wireKey: 'init-me',
  },
  {
    id: 'm-me-refresh',
    step: 'refresh',
    from: 'browser',
    to: 'duster',
    label: 'GET /me',
    detail: 'refresh() — slides the TTL, swaps the upstream token if near expiry',
    mode: 'observed',
    wireKey: 'refresh-me',
  },
  {
    id: 'm-revoke',
    step: 'logout',
    from: 'duster',
    to: 'authos',
    label: 'POST /oauth/revoke',
    detail: 'RFC 7009 — best-effort, cascades to the access tokens',
    mode: 'narrated',
  },
  {
    id: 'm-logout',
    step: 'logout',
    from: 'browser',
    to: 'duster',
    label: 'POST /logout',
    detail: 'purges duster:token:* · drops the session · clears the cookie',
    mode: 'observed',
    wireKey: 'logout',
  },
]

/**
 * Activation bars — the stretch of a lifeline where that actor is doing work, drawn as a
 * narrow bar over the lifeline between two messages (inclusive).
 */
export interface Activation {
  actor: Actor
  from: string
  to: string
}

export const ACTIVATIONS: Activation[] = [
  { actor: 'duster', from: 'm-start', to: 'm-authorize' }, // mint the PKCE verifier, 302
  { actor: 'authos', from: 'm-login', to: 'm-code' }, // login, consent, issue the code
  { actor: 'duster', from: 'm-token', to: 'm-cookie' }, // exchange, verify, store, set-cookie
  { actor: 'duster', from: 'm-me-init', to: 'm-me-init' }, // answer /me
  { actor: 'duster', from: 'm-me-refresh', to: 'm-me-refresh' }, // answer /me, swap upstream token
  { actor: 'duster', from: 'm-revoke', to: 'm-logout' }, // revoke upstream, purge, clear
]

/** `opt` / `ref` frames — a labelled bracket around the messages that share a `frame` value. */
export interface Frame {
  label: string
  from: string
  to: string
}

export const FRAMES: Frame[] = (() => {
  const out: Frame[] = []
  let run: { label: string; first: string; last: string } | null = null
  for (const m of MESSAGES) {
    if (m.frame && run?.label === m.frame) {
      run.last = m.id
    } else {
      if (run) out.push({ label: run.label, from: run.first, to: run.last })
      run = m.frame ? { label: m.frame, first: m.id, last: m.id } : null
    }
  }
  if (run) out.push({ label: run.label, from: run.first, to: run.last })
  return out
})()

/** Token-channel edges, keyed to the message after which they occur. */
export interface TokenEdge {
  afterMessage: string
  edge: 'rise' | 'rearm' | 'fall'
  label: string
}

export const TOKEN_EDGES: TokenEdge[] = [
  { afterMessage: 'm-cookie', edge: 'rise', label: 'established' },
  { afterMessage: 'm-me-refresh', edge: 'rearm', label: 're-armed' },
  { afterMessage: 'm-logout', edge: 'fall', label: 'purged' },
]

export const stepOf = (id: StepId): Step => STEPS.find((s) => s.id === id)!
export const messagesOf = (step: StepId): Message[] =>
  MESSAGES.filter((m) => m.step === step)
