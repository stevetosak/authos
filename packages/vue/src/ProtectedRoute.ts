import { defineComponent } from 'vue'
import { useDuster } from './useDuster.js'

/**
 * Gate a subtree on the Duster session. Purely presentational — the redirect on a missing session
 * is driven by the plugin's `onUnauthenticated` (default `'redirect'`), not by this component.
 *
 * Slots:
 * - `default` — rendered when authenticated
 * - `loading` — rendered while the first `/me` is in flight (default: nothing)
 * - `fallback` — rendered when not authenticated; pair with `onUnauthenticated: 'ignore'` for an
 *   in-page login prompt (default: nothing)
 * - `errorFallback` — rendered on a server / network error while not already authenticated (a
 *   stale-refresh error while authenticated still renders `default`); default: falls through to
 *   `loading` / `fallback`
 *
 * ```ts
 * h(ProtectedRoute, null, {
 *   default: () => h(Dashboard),
 *   loading: () => h(Spinner),
 *   fallback: () => h('button', { onClick: login }, 'Sign in'),
 * })
 * ```
 */
export const ProtectedRoute = defineComponent({
  name: 'DusterProtectedRoute',
  setup(_props, { slots }) {
    const { status, error } = useDuster()
    return () => {
      if (status.value === 'authenticated') return slots.default?.()
      if (error.value && slots.errorFallback) return slots.errorFallback()
      if (status.value === 'loading') return slots.loading?.()
      return slots.fallback?.()
    }
  },
})

/** Alias of {@link ProtectedRoute}. */
export const Protected = ProtectedRoute
