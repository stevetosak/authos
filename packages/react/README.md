# @authoss/duster-react

React bindings for [Duster](https://github.com/stevetosak/authos/tree/master/duster) — a thin layer
over [`@authoss/duster-core`](../core). Session state via `useSyncExternalStore`, no router
dependency.

```
npm i @authoss/duster-react
```

## Usage

```tsx
import { DusterProvider, useDuster, ProtectedRoute } from '@authoss/duster-react'

function App() {
  return (
    <DusterProvider clientId="app_123" onUnauthenticated="redirect">
      <Routes />
    </DusterProvider>
  )
}

function Nav() {
  const { user, status, login, logout } = useDuster()
  if (status === 'loading') return null
  return status === 'authenticated'
    ? <button onClick={() => logout()}>Sign out {user!.email}</button>
    : <button onClick={() => login()}>Sign in</button>
}

function Dashboard() {
  return (
    <ProtectedRoute loading={<Spinner />}>
      <SecretStuff />
    </ProtectedRoute>
  )
}
```

## `<DusterProvider>`

Config as flat props (`clientId` required; `baseUrl`, `basePath`, `onUnauthenticated`,
`postLogoutRedirect`, `revalidateOnFocus`, `revalidateOnReconnect`, `fetch`), or the whole object via
`config={…}`. Runs the first `/me` on mount; safe under StrictMode and multiple providers (one
client per `clientId`). Changing `clientId` at runtime isn't supported — remount with
`key={clientId}`.

## `useDuster()`

`{ user, status, login, logout, refresh, error }`. `status` is `'loading' | 'authenticated' |
'unauthenticated'`. `error` is set on a server / network failure and does **not** by itself mean
logged-out. Throws if used outside a provider.

## `<ProtectedRoute>` (alias `<Protected>`)

Presentational: `loading` → `children` (authenticated) → `fallback` (unauthenticated) →
`errorFallback` (server/network error while not authenticated). The redirect on a missing session is
driven by the provider's `onUnauthenticated`, not this component — for an in-page login prompt, pass
`fallback` and set `onUnauthenticated="ignore"`.

## Router recipes

No router is bundled. For react-router v7, gate in a `loader`:

```ts
const dusterClient = getOrCreateDusterClient({ clientId: 'app_123' })
export async function protectedLoader() {
  await dusterClient.init()
  if (dusterClient.getSnapshot().status !== 'authenticated') {
    throw redirect('/login')
  }
  return null
}
```

MIT © Authos
