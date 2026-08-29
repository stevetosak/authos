import type { DusterClient, DusterConfig } from './types.js'
import { createDusterClient } from './client.js'

/** One client per `clientId` — dedupes a double-mounted provider and its double `/me`. */
const registry = new Map<string, DusterClient>()
const configs = new Map<string, DusterConfig>()

function sameConfig(a: DusterConfig, b: DusterConfig): boolean {
  return (
    a.baseUrl === b.baseUrl &&
    a.basePath === b.basePath &&
    a.postLogoutRedirect === b.postLogoutRedirect &&
    a.onUnauthenticated === b.onUnauthenticated &&
    a.revalidateOnFocus === b.revalidateOnFocus &&
    a.revalidateOnReconnect === b.revalidateOnReconnect
  )
}

export function getOrCreateDusterClient(config: DusterConfig): DusterClient {
  const existing = registry.get(config.clientId)
  if (existing) {
    const prev = configs.get(config.clientId)
    if (prev && !sameConfig(prev, config)) {
      console.warn(
        `duster: a client for "${config.clientId}" already exists with a different config; keeping the first`,
      )
    }
    return existing
  }
  const client = createDusterClient(config)
  registry.set(config.clientId, client)
  configs.set(config.clientId, config)
  return client
}

/** Test / HMR helper — forget all cached clients (does not `destroy()` them). */
export function resetDusterRegistry(): void {
  registry.clear()
  configs.clear()
}
