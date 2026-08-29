import { computed, inject, onScopeDispose, shallowRef } from 'vue'
import type { ComputedRef } from 'vue'
import type { DusterError, DusterStatus, DusterUser } from '@authoss/duster-core'
import { DusterInjectionKey } from './plugin.js'

export interface UseDuster {
  /** Normalized userinfo, or `null` when not authenticated. */
  user: ComputedRef<DusterUser | null>
  status: ComputedRef<DusterStatus>
  /** Non-null after a server / network failure (see `kind`). Not an auth failure on its own. */
  error: ComputedRef<DusterError | null>
  /** Start the login redirect. Synchronous — ends in a full-page navigation. */
  login: (opts?: { returnTo?: string }) => void
  /** POST `/logout`, clear local state, navigate away. */
  logout: (opts?: { redirectTo?: string }) => Promise<void>
  /** Force a `/me` re-check. */
  refresh: () => Promise<void>
}

/**
 * Reactive Duster session state. Call in `setup()` (or another composable). Bridges the core's
 * observable store into a `shallowRef` and tears the subscription down with the effect scope.
 */
export function useDuster(): UseDuster {
  const client = inject(DusterInjectionKey)
  if (!client) {
    throw new Error('useDuster() requires the Duster plugin — app.use(createDuster({ clientId }))')
  }

  const snapshot = shallowRef(client.getSnapshot())
  onScopeDispose(client.subscribe(() => {
    snapshot.value = client.getSnapshot()
  }))

  return {
    user: computed(() => snapshot.value.user),
    status: computed(() => snapshot.value.status),
    error: computed(() => snapshot.value.error),
    login: (opts) => client.login(opts),
    logout: (opts) => client.logout(opts),
    refresh: () => client.refresh(),
  }
}
