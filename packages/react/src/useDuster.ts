import { useCallback, useContext, useSyncExternalStore } from 'react'
import type { DusterError, DusterStatus, DusterUser } from '@authoss/duster-core'
import { DusterContext } from './context.js'

export interface UseDuster {
  /** Normalized userinfo, or `null` when not authenticated. */
  user: DusterUser | null
  status: DusterStatus
  /** Non-null after a server / network failure (see `kind`). Not an auth failure on its own. */
  error: DusterError | null
  /** Start the login redirect. Synchronous — ends in a full-page navigation. */
  login: (opts?: { returnTo?: string }) => void
  /** POST `/logout`, clear local state, navigate away. */
  logout: (opts?: { redirectTo?: string }) => Promise<void>
  /** Force a `/me` re-check. */
  refresh: () => Promise<void>
}

export function useDuster(): UseDuster {
  const client = useContext(DusterContext)
  if (!client) {
    throw new Error('useDuster() must be used inside a <DusterProvider>')
  }

  const subscribe = useCallback((cb: () => void) => client.subscribe(cb), [client])
  const getSnapshot = useCallback(() => client.getSnapshot(), [client])
  const getServerSnapshot = useCallback(() => client.getServerSnapshot(), [client])

  const snapshot = useSyncExternalStore(subscribe, getSnapshot, getServerSnapshot)

  const login = useCallback(
    (opts?: { returnTo?: string }) => client.login(opts),
    [client],
  )
  const logout = useCallback(
    (opts?: { redirectTo?: string }) => client.logout(opts),
    [client],
  )
  const refresh = useCallback(() => client.refresh(), [client])

  return {
    user: snapshot.user,
    status: snapshot.status,
    error: snapshot.error,
    login,
    logout,
    refresh,
  }
}
