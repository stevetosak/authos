# @authoss/duster-core

Framework-agnostic browser client for the [Duster](https://github.com/stevetosak/authos/tree/master/duster)
BFF. It owns the whole Duster wire contract — session read, silent-refresh awareness, CSRF handling,
logout, the login redirect — behind a small observable-store interface. The React / Vue / Angular
adapters are thin wrappers over this; you can also use it directly, or as a `<script>` global.

```
npm i @authoss/duster-core
```

## Direct use

```ts
import { getOrCreateDusterClient } from '@authoss/duster-core'

const duster = getOrCreateDusterClient({
  clientId: 'app_123',
  // baseUrl: 'https://auth.example.com',   // set only for a cross-origin (tier 1) app
  onUnauthenticated: 'redirect',            // 'redirect' | 'ignore' | (ctx) => void
})

await duster.init()                         // first GET /me; safe to call repeatedly

const { user, status, error } = duster.getSnapshot()
// status: 'loading' | 'authenticated' | 'unauthenticated'
// user:   normalized OIDC claims + user.raw (every /me value, verbatim)

duster.subscribe(() => render(duster.getSnapshot()))

duster.login()                              // → window.location = /duster/api/v1/oauth/start
await duster.logout()                       // POST /logout, clear state, navigate away
```

## `<script>` global

```html
<script src="https://unpkg.com/@authoss/duster-core/dist/duster.global.js"></script>
<script>
  const duster = Duster.getOrCreateDusterClient({ clientId: 'app_123' })
  duster.init().then(() => {
    const { status, user } = duster.getSnapshot()
    if (status === 'authenticated') document.body.dataset.user = user.email
  })
</script>
```

## Behaviour notes

- **No polling.** Duster slides the session TTL and silent-refreshes the upstream token on every
  `/me`, so a timer buys nothing. Opt in to `revalidateOnFocus` / `revalidateOnReconnect` for a
  single re-check on tab focus / reconnect.
- **A 5xx or network failure is not a logout.** `status` stays where it was and `error` is set; the
  initial load stays `loading` and retries with backoff. Only a real `401` moves you to
  `unauthenticated` and fires `onUnauthenticated`.
- **The session cookie is `HttpOnly`** — the SDK never reads it. All session state comes from `/me`.
- **`login()` is synchronous** and ends in a full-page navigation. Don't `await` it.
- On a failed login Duster lands the browser on `<success_url origin>/error` — give your app an
  `/error` route. `readDusterError()` reads `?error=` off the URL there.

## Tiers

| Tier | Config | Duster setup |
|------|--------|--------------|
| 0 — same-origin | `{ clientId }` | `/duster` reverse-proxied onto your SPA's origin |
| 1 — cross-origin | `{ clientId, baseUrl: 'https://auth…' }` | app registered `allowed_origins` (`dstr apps configure --allowed-origins`) |

## API

`getOrCreateDusterClient(config)` · `createDusterClient(config)` · `normalizeUser(body)` ·
`buildStartUrl(config)` · `buildLogoutUrl(config)` · `buildMeUrl(config)` · `readDusterError(search?)`

See the TypeScript types for the full `DusterConfig` / `DusterClient` / `DusterUser` shape.

MIT © Authos
