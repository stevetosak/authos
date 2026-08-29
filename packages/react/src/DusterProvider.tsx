import { useEffect, useMemo, type ReactNode } from 'react'
import { getOrCreateDusterClient, type DusterConfig } from '@authoss/duster-core'
import { DusterContext } from './context.js'

export interface DusterProviderProps extends Partial<DusterConfig> {
  /** OAuth client id of the Duster-registered app. Required. */
  clientId: string
  children: ReactNode
  /** Escape hatch — pass the whole config object instead of individual props. Wins over props. */
  config?: DusterConfig
}

/**
 * Provides one {@link DusterClient} to the tree and runs its first `/me` on mount.
 *
 * The client is keyed by `clientId` (+ transport config) via `getOrCreateDusterClient`, so multiple
 * providers, React StrictMode's double-mount, and re-renders all share a single client and a single
 * in-flight `/me`. Changing `clientId` at runtime is not supported — remount with `key={clientId}`.
 */
export function DusterProvider({ children, config, ...rest }: DusterProviderProps): ReactNode {
  const resolved: DusterConfig = config ?? (rest as DusterConfig)

  const client = useMemo(
    () => getOrCreateDusterClient(resolved),
    // Identity of the client only needs to change when the transport target does.
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [resolved.clientId, resolved.baseUrl, resolved.basePath],
  )

  useEffect(() => {
    void client.init()
  }, [client])

  return <DusterContext.Provider value={client}>{children}</DusterContext.Provider>
}
