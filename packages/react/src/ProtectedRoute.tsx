import type { ReactNode } from 'react'
import { useDuster } from './useDuster.js'

export interface ProtectedRouteProps {
  children: ReactNode
  /** Shown while the first `/me` is in flight. Default: nothing. */
  loading?: ReactNode
  /**
   * Shown when the user is not authenticated. Provide this (and set
   * `onUnauthenticated: 'ignore'` on the provider) to render an in-page login prompt instead of
   * redirecting. Default: nothing.
   */
  fallback?: ReactNode
  /**
   * Shown when there is a server / network `error` and the user is not already authenticated
   * (a stale-refresh error while authenticated still renders `children`). Default: falls through to
   * `loading` / `fallback`.
   */
  errorFallback?: ReactNode
}

/**
 * Gate a subtree on the Duster session. Purely presentational — the redirect on a missing session
 * is driven by the provider's `onUnauthenticated` (default `'redirect'`), not by this component.
 */
export function ProtectedRoute({
  children,
  loading = null,
  fallback = null,
  errorFallback,
}: ProtectedRouteProps): ReactNode {
  const { status, error } = useDuster()

  if (status === 'authenticated') return children
  if (error && errorFallback !== undefined) return errorFallback
  if (status === 'loading') return loading
  return fallback
}

/** Alias of {@link ProtectedRoute}. */
export const Protected = ProtectedRoute
