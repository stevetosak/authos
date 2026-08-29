# @authoss/duster-vue

Vue 3 bindings for [Duster](https://github.com/stevetosak/authos/tree/master/duster) — a thin layer
over [`@authoss/duster-core`](../core). Reactive session state via `shallowRef`, no router
dependency.

```
npm i @authoss/duster-vue
```

## Usage

```ts
import { createApp } from 'vue'
import { createDuster } from '@authoss/duster-vue'
import App from './App.vue'

createApp(App)
  .use(createDuster({ clientId: 'app_123', onUnauthenticated: 'redirect' }))
  .mount('#app')
```

```ts
import { defineComponent, h } from 'vue'
import { useDuster, ProtectedRoute } from '@authoss/duster-vue'

const Nav = defineComponent(() => {
  const { user, status, login, logout } = useDuster()
  return () => {
    if (status.value === 'loading') return null
    return status.value === 'authenticated'
      ? h('button', { onClick: () => logout() }, `Sign out ${user.value!.email}`)
      : h('button', { onClick: () => login() }, 'Sign in')
  }
})

const Dashboard = defineComponent(() =>
  () => h(ProtectedRoute, null, {
    loading: () => h(Spinner),
    default: () => h(SecretStuff),
  }),
)
```

## `createDuster(config)`

The Vue plugin. `config` is a `DusterConfig` — `clientId` (required), `baseUrl`, `basePath`,
`onUnauthenticated` (`'redirect' | 'ignore' | fn`), `postLogoutRedirect`, `revalidateOnFocus`,
`revalidateOnReconnect`, `fetch`. Provides one client for the app and runs the first `/me` on
install; safe to call twice with the same `clientId` (one client, one in-flight `/me`).

## `useDuster()`

Returns `{ user, status, error, login, logout, refresh }` — `user` / `status` / `error` are
`ComputedRef`s, so read them as `status.value` in `setup()` or `status` in a template. `status` is
`'loading' | 'authenticated' | 'unauthenticated'`. `error` is set on a server / network failure and
does **not** by itself mean logged-out. Call it in `setup()` (or another composable); throws if the
plugin isn't installed. The store subscription is torn down with the component's effect scope.

## `<ProtectedRoute>` (alias `<Protected>`)

Presentational. Renders, in order: the `default` slot (authenticated) → `errorFallback` slot
(server/network error while not authenticated) → `loading` slot → `fallback` slot. The redirect on
a missing session is driven by the plugin's `onUnauthenticated`, not this component — for an in-page
login prompt, provide `fallback` and set `onUnauthenticated: 'ignore'`.

## Router recipe

No router is bundled. For vue-router, gate in a navigation guard:

```ts
import { getOrCreateDusterClient } from '@authoss/duster-core'

const duster = getOrCreateDusterClient({ clientId: 'app_123' })

router.beforeEach(async (to) => {
  if (!to.meta.protected) return true
  await duster.init()
  return duster.getSnapshot().status === 'authenticated' ? true : { name: 'login' }
})
```

MIT © Authos
