import { getOrCreateDusterClient } from '@authoss/duster-core'
import type { DusterClient, DusterConfig } from '@authoss/duster-core'
import type { App, InjectionKey, Plugin } from 'vue'

/**
 * Injection key for the shared {@link DusterClient}. Exported for advanced use (a component that
 * calls `inject(DusterInjectionKey)` directly, or a test that provides a stand-in). Normal apps
 * never touch it — `useDuster()` resolves it.
 */
export const DusterInjectionKey: InjectionKey<DusterClient> = Symbol('duster-client')

/**
 * Vue plugin. Registers one {@link DusterClient} for the app and runs its first `/me`.
 *
 * ```ts
 * import { createApp } from 'vue'
 * import { createDuster } from '@authoss/duster-vue'
 *
 * createApp(App).use(createDuster({ clientId: 'app_123' })).mount('#app')
 * ```
 *
 * The client is keyed by `clientId` (+ transport config) via `getOrCreateDusterClient`, so calling
 * this twice with the same `clientId` yields one client and one in-flight `/me`. Changing
 * `clientId` at runtime is not supported — recreate the app or use a fresh client.
 */
export function createDuster(config: DusterConfig): Plugin {
  const client = getOrCreateDusterClient(config)
  return {
    install(app: App) {
      app.provide(DusterInjectionKey, client)
      void client.init()
    },
  }
}
